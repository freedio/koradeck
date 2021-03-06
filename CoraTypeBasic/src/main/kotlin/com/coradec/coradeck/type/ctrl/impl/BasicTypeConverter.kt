package com.coradec.coradeck.type.ctrl.impl

import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.type.ctrl.TypeConverter
import com.coradec.coradeck.type.trouble.TypeConversionException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.core.util.name
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

@Suppress("UNCHECKED_CAST")
abstract class BasicTypeConverter<T : Any>(val type: KType) : TypeConverter<T> {
    constructor(klass: KClass<T>): this(klass.createType(klass.typeParameters.map { KTypeProjection.STAR }))
    private val klass = type.classifier as KClass<*>
    override fun handles(klass: KClass<*>): Boolean = klass.isSuperclassOf(klass)
    override fun handles(type: KType): Boolean = type.isSupertypeOf(type)

    override fun convert(value: Any?): T? = when (value) {
        null -> null
        in type -> value as T
        is String -> decode(value)
        else -> convertFrom(value) ?: throw TypeConversionException(
            "Failed to convert %s value %s to type %s".format(value::class.java.name, value.formatted, type.name)
        )
    }

    override fun decode(value: String?): T? = when (value) {
        null -> null
        else -> decodeFrom(value) ?: throw TypeConversionException(
            "Failed to decode \"%s\" to type %s".format(value, type.name)
        )
    }

    protected abstract fun decodeFrom(value: String): T?
    protected abstract fun convertFrom(value: Any): T?
}
