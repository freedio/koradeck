/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter

class DoubleConverter : BasicTypeConverter<Double>(Double::class) {
    override fun decodeFrom(value: String): Double? = value.toDoubleOrNull()

    override fun convertFrom(value: Any): Double? = when (value) {
        is Number -> value.toDouble()
        else -> null
    }
}
