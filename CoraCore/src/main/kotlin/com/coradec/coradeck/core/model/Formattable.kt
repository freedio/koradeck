package com.coradec.coradeck.core.model

interface Formattable {
    fun format(known: Set<Any?>): String
}
