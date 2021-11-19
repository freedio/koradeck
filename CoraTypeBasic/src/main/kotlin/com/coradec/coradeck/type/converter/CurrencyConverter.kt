/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import java.util.*

class CurrencyConverter: BasicTypeConverter<Currency>(Currency::class) {
    override fun decodeFrom(value: String): Currency? = try {
        Currency.getInstance(value)
    } catch (e: IllegalArgumentException) {
        null
    }

    override fun convertFrom(value: Any): Currency? {
        TODO("Not yet implemented")
    }
}
