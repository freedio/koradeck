package com.coradec.coradeck.dir.trouble

import kotlin.reflect.KClass

class UnsupportedTypeException(val klass: KClass<*>) : DirectoryException()
