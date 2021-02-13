package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.Text
import com.coradec.coradeck.text.model.TextBase

data class StandardTextBase(private val entries: Map<String, Text>) : TextBase {
    override fun get(name: String): Text? = entries[name]
}
