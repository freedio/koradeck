package com.coradec.coradeck.type.ctrl.impl

import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.type.ctrl.TypeConverter
import com.coradec.coradeck.type.trouble.TypeConversionException
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import com.coradec.coradeck.core.util.contains

abstract class BasicTypeConverter<T: Any>(val type: KClass<T>) : TypeConverter<T> {
    override fun handles(type: KClass<*>): Boolean = type.isSuperclassOf(type)
    override fun convert(value: Any?): T? = when (value) {
        null -> null
        in type -> value as T
        is String -> decode(value)
        else -> convertFrom(value)
                ?: throw TypeConversionException("Failed to convert value %s to type %s".format(value.formatted, type.qualifiedName))
    }

    override fun decode(value: String?): T? = when(value) {
        null -> null
        else -> decodeFrom(value)
                ?: throw TypeConversionException("Failed to decode \"%s\" to type %s".format(value, type.qualifiedName))
    }

    protected abstract fun decodeFrom(value: String): T?
    protected abstract fun convertFrom(value: Any): T?
}
