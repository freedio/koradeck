/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter

class IntConverter(): BasicTypeConverter<Int>(Int::class) {
    override fun decodeFrom(value: String): Int = value.toInt()
    override fun convertFrom(value: Any): Int? = when (value) {
        is Number -> value.toInt()
        else -> null
    }
}
