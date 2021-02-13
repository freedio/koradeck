package com.coradec.coradeck.text.module

import com.coradec.coradeck.dir.model.module.CoraModule
import com.coradec.coradeck.text.model.TextBase

object CoraText : CoraModule<CoraTextAPI>() {
    /** Retrieves the text base of the specified context. */
    fun getTextBase(context: String): TextBase = impl.getTextBase(context,)
}
