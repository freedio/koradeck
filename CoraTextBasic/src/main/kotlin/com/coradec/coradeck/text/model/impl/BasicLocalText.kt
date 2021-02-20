package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.TextBases
import java.util.*

class BasicLocalText(element: TextElement) : BasicConText(element), LocalText {
    private val base = element.base
    override fun get(vararg args: Any): String = get(Locale.getDefault(), args)
    override fun get(locale: Locale, vararg args: Any): String =
        TextBases.loadLocalizedTextBase(context, locale)[name] ?: MissingLocalText(context, name).content
}
