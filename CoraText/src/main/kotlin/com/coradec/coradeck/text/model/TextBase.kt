package com.coradec.coradeck.text.model

interface TextBase {
    operator fun get(name: String): NamedText?
    fun getLocalText(name: String): LocalText?
}
