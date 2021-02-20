package com.coradec.coradeck.conf.trouble

import kotlin.reflect.KType

class PropertyUndefinedException(val name: String, val type: KType) : ConfigurationException()
