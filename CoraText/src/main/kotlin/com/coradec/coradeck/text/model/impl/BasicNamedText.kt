package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.NamedText

abstract class BasicNamedText(override val name: String): BasicText(), NamedText
