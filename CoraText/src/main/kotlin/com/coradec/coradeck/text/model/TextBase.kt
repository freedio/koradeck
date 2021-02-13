package com.coradec.coradeck.text.model

import com.coradec.coradeck.text.module.CoraText

interface TextBase {
    operator fun get(name: String): Text?

    companion object {
        operator fun invoke(context: String) = CoraText.getTextBase(context)
    }
}
