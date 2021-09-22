/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.trouble

import kotlin.reflect.KClass

class TypeConversionException(message: String?) : TypeException(message, null) {
    constructor(value: Any, type: KClass<*>) : this("Failed to convert \"$value\" to ${type.qualifiedName}")
}
