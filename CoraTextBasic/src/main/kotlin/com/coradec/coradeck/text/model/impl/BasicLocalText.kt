/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.TextBases
import java.util.*

class BasicLocalText(element: TextElement) : BasicConText(element), LocalText {
    override fun get(vararg args: Any): String = get(Locale.getDefault(), *args)
    override fun get(locale: Locale, vararg args: Any): String =
        TextBases.loadLocalizedTextBase(context, locale)[name]
            ?.let { if (args.isEmpty()) it else it.format(*args) }
            ?: MissingLocalText(context, name).content
}
