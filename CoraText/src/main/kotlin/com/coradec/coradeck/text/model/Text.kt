package com.coradec.coradeck.text.model

import java.util.*

interface Text {
    operator fun get(locale: Locale, vararg args: Any): String
    fun content(locale: Locale): String = get(locale)
    operator fun get(vararg args: Any): String = get(Locale.getDefault(), args)
    val content: String get() = content(Locale.getDefault())
}
