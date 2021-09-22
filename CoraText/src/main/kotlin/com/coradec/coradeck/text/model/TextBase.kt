/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model

import com.coradec.coradeck.text.module.CoraText

interface TextBase {
    operator fun get(name: String): NamedText?
    fun getLocalText(name: String): LocalText?

    companion object {
        operator fun invoke(context: String): TextBase = CoraText.getTextBase(context)
    }
}
