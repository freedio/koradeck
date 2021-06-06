/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.module

import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.TextBase
import com.coradec.coradeck.text.model.TextBases
import com.coradec.coradeck.text.model.impl.MissingLocalText

class CoraTextImpl: CoraTextAPI {
    override fun createLocalText(context: String, name: String): LocalText =
        TextBases.byContext(context).getLocalText(name) ?: MissingLocalText(context, name)

    override fun getTextBase(context: String): TextBase {
        TODO("Not yet implemented")
    }
}
