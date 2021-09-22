/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.NamedText
import com.coradec.coradeck.text.model.TextBase
import java.util.concurrent.ConcurrentHashMap

class LocalizedTextBase: TextBase {
    val texts = ConcurrentHashMap<String, TextElement>()
    fun putAll(map: Map<String, TextElement>) = texts.putAll(map)
    override fun get(name: String): NamedText? = texts[name]?.let { BasicNamedText(it) }
    override fun getLocalText(name: String): LocalText? = texts[name]?.let { BasicLocalText(it) }
}
