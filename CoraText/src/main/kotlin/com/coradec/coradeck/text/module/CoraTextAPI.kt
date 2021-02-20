package com.coradec.coradeck.text.module

import com.coradec.coradeck.dir.model.module.CoraModuleAPI
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.TextBase
import java.util.*

interface CoraTextAPI: CoraModuleAPI {
    /** Creates a localized text with the specified name in the specified context. */
    fun createLocalText(context: String, name: String): LocalText
}
