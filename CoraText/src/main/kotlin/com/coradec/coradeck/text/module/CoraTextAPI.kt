package com.coradec.coradeck.text.module

import com.coradec.coradeck.dir.model.module.CoraModuleAPI
import com.coradec.coradeck.text.model.TextBase
import java.util.*

interface CoraTextAPI: CoraModuleAPI {
    /** Retrieves the text base of the specified context. */
    fun getTextBase(context: String): TextBase
}
