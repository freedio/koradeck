/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class IntConverter(): BasicTypeConverter<Int>(Int::class) {
    override fun decodeFrom(value: String): Int = value.toInt()
    override fun convertFrom(value: Any): Int? = when (value) {
        is Number -> value.toInt()
        else -> null
    }
}
