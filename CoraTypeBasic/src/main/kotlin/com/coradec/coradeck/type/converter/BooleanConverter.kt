package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import java.lang.IllegalArgumentException

class BooleanConverter: BasicTypeConverter<Boolean>(Boolean::class) {
    override fun decodeFrom(value: String): Boolean? = when(value.toLowerCase()) {
        "yes", "on", "true", "+" -> true
        "no", "off", "false", "-", "−" -> false
        else -> throw IllegalArgumentException("Not a representation of Boolean: \"$value\"!")
    }

    override fun convertFrom(value: Any): Boolean? = when (value) {
        is Boolean -> value
        is Float -> value != 0.0f
        is Double -> value != 0.0
        is Number -> value.toLong() != 0L
        else -> null
    }
}