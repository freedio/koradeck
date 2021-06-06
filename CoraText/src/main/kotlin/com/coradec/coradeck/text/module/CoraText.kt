package com.coradec.coradeck.text.module

import com.coradec.coradeck.dir.model.module.CoraModule
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.TextBase

object CoraText : CoraModule<CoraTextAPI>() {
    /** Creates a localized text with the specified name in the specified context. */
    fun createLocalText(context: String, name: String): LocalText = impl.createLocalText(context, name)
    /** Returns the text base containing the properties of the specified context. */
    fun getTextBase(context: String): TextBase = impl.getTextBase(context)
}
