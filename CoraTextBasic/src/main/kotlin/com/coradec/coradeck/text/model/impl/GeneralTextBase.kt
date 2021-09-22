/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.NamedText
import com.coradec.coradeck.text.model.TextBase

data class GeneralTextBase(private val entries: Map<String, TextElement>) : TextBase {
    override fun get(name: String): NamedText? = entries[name]?.let { BasicNamedText(it) }
    override fun getLocalText(name: String): LocalText? = entries[name]?.let { BasicLocalText(it) }
}
