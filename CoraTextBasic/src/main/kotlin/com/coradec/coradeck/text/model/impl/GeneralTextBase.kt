package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.*
import java.util.*

data class GeneralTextBase(private val entries: Map<String, TextElement>) : TextBase {
    override fun get(name: String): NamedText? = entries[name]?.let { BasicNamedText(it) }
    override fun getLocalText(name: String): LocalText? = entries[name]?.let { BasicLocalText(it) }
}
