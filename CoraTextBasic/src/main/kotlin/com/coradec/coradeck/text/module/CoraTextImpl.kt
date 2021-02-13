/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.module

import com.coradec.coradeck.text.model.TextBase
import com.coradec.coradeck.text.model.TextBases
import java.util.*

class CoraTextImpl: CoraTextAPI {
    override fun getTextBase(context: String, locale: Locale): TextBase = TextBases.byContext(context, locale)
}
