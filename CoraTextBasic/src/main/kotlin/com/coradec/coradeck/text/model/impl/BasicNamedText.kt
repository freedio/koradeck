package com.coradec.coradeck.text.model.impl

import com.coradec.coradeck.text.model.NamedText

open class BasicNamedText(element: TextElement): NamedText {
    override val name: String = element.name
    override val content: String = element.content
    override fun get(vararg args: Any): String = content.format(args)
}
