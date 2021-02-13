package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.ConText
import com.coradec.coradeck.text.model.Text
import com.coradec.coradeck.text.model.TextBase
import com.coradec.coradeck.text.trouble.ConTextNotFoundException
import java.util.*

class BasicConText(override val context: String, override val name: String) : BasicNamedText(name), ConText {
    private val textBase = TextBase(context)
    override fun content(locale: Locale) = textBase["$context.$name"]?.content(locale)
        ?: throw ConTextNotFoundException(context, locale, name)
    override fun get(locale: Locale, vararg args: Any): String = content(locale).format(args)
}
