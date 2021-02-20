package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.ConText

open class BasicConText(element: TextElement): BasicNamedText(element), ConText {
    override val context: String = element.context
}
