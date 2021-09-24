/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.core.util.LETTER_ESCAPES
import com.coradec.coradeck.core.util.map
import com.coradec.coradeck.core.util.plusAssign
import com.coradec.coradeck.core.util.text
import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import com.fasterxml.jackson.databind.node.ArrayNode
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

class List_of_String_Converter: BasicTypeConverter<List<String>>(
    List::class.createType(listOf(KTypeProjection(KVariance.OUT, String::class.starProjectedType)))
) {
    override fun decodeFrom(value: String): List<String>? = when {
        value.trimStart().startsWith("{") -> decodeStandardStringListSyntax(value.trim())
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    override fun convertFrom(value: Any): List<String>? = when (value) {
        is ArrayNode -> value.map { it.text }.toList()
        is Collection<*> -> when {
            value.isEmpty() -> emptyList()
            value.first() is String -> (value as Collection<String>).toList()
            else -> value.map {  it.toString() }
        }
        else -> null
    }

    private fun decodeStandardStringListSyntax(input: String): List<String> {
        if (input.last() != ']') throw IllegalArgumentException("Standard list representation must end with '}'")
        val x = input.drop(1).dropLast(1)
        var quote = '\u0000'
        var escaped = false
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var hexcape: MutableList<Char>? = null
        fun ship() {
            result += current.toString()
            current.setLength(0)
        }

        fun handleHexcape(c: Char) {
            val i = "0123456789abcdef".indexOf(c)
            if (i != -1) hexcape!! += c
        }

        fun handleEscape(c: Char) {
            when {
                hexcape != null -> handleHexcape(c)
                c == 'u' -> hexcape = mutableListOf()
                c in LETTER_ESCAPES -> {
                    current.append("\u0007\b\u000c\n\r\t\u0000"[LETTER_ESCAPES.indexOf(c)])
                    escaped = false
                }
            }
        }
        x.forEach { c ->
            when {
                escaped -> handleEscape(c)
                c == quote -> quote = '\u0000'
                c in "'\"" -> quote = c
                quote != '\u0000' -> current += c
                c == '\\' -> escaped = true
                c in ",;" -> ship()
                else -> current += c
            }
            if (quote != '\u0000') throw IllegalStateException("Open quote!")
            if (escaped) throw IllegalStateException("Open escape sequence!")
        }
        return result.toList()
    }
}