package com.coradec.coradeck.type.trouble

import kotlin.reflect.KClass

class TypeConverterNotFoundException(val type: String? = null, val converterClass: String) : TypeException()
