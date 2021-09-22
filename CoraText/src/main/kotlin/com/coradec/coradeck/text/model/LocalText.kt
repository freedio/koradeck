/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model

import com.coradec.coradeck.core.util.caller2
import com.coradec.coradeck.text.module.CoraText
import java.util.*

interface LocalText: ConText {
    operator fun get(locale: Locale, vararg args: Any): String
    override operator fun get(vararg args: Any) = get(Locale.getDefault(), args)
    fun content(locale: Locale): String = get(locale)
    companion object {
        operator fun invoke(name: String): LocalText = CoraText.createLocalText(caller2.className, name)
        operator fun invoke(context: String, name: String): LocalText = CoraText.createLocalText(context, name)
    }
}
