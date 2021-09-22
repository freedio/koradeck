/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.LocalText
import java.util.*

class MissingLocalText(override val context: String, override val name: String) : LocalText {
    override fun get(locale: Locale, vararg args: Any): String = "<unknown text literal: $context:$name>"
}
