package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter

class ClassConverter: BasicTypeConverter<Class<*>>(Class::class) {
    override fun decodeFrom(value: String): Class<*>? = Class.forName(value)
    override fun convertFrom(value: Any): Class<*>? = null
}