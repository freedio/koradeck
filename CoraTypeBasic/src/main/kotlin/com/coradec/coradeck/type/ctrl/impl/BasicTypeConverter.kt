/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.ctrl.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.core.util.name
import com.coradec.coradeck.type.ctrl.TypeConverter
import com.coradec.coradeck.type.trouble.TypeConversionException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.isSupertypeOf

@Suppress("UNCHECKED_CAST")
abstract class BasicTypeConverter<T : Any?>(val type: KType) : Logger(), TypeConverter<T> {
    constructor(klass: KClass<*>, nullable: Boolean = false):
            this(klass.createType(klass.typeParameters.map { KTypeProjection.STAR }, nullable))
    private val klass = type.classifier as KClass<*>
    override fun handles(klass: KClass<*>): Boolean = klass.isSuperclassOf(klass)
    override fun handles(type: KType): Boolean = type.isSupertypeOf(type)

    override fun convert(value: Any?): T = when (value) {
        null -> null as T
        in type -> value as T
        is String -> decode(value)
        else -> convertFrom(value) ?: throw TypeConversionException(
            "%s: Failed to convert %s value %s to type %s".format(classname, value::class.java.name, value.formatted, type.name)
        ).apply { error(this) }
    }

    override fun decode(value: String?): T = when (value) {
        null -> null as T
        else -> decodeFrom(value) ?: throw TypeConversionException("Failed to decode \"%s\" to type %s".format(value, type.name))
            .apply { error(this) }
    }

    /** Tries to decode the specified String value into an instance of the derived type, returning null if not possible. */
    protected abstract fun decodeFrom(value: String): T?
    /** Tries to convert the specified value into an instance of the derived type, returning null if not possible. */
    protected abstract fun convertFrom(value: Any): T?
}
