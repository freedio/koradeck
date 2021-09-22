/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.module

import com.coradec.coradeck.dir.model.module.CoraModuleAPI
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.TextBase

interface CoraTextAPI: CoraModuleAPI {
    /** Creates a localized text with the specified name in the specified context. */
    fun createLocalText(context: String, name: String): LocalText
    /** Returns the text base containing the properties of the specified context. */
    fun getTextBase(context: String): TextBase
}
