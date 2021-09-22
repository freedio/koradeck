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
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

class Map_of_String_and_String_Converter : BasicTypeConverter<Map<String, String>>(Map::class.createType(listOf(
    KTypeProjection(KVariance.OUT, String::class.starProjectedType),
    KTypeProjection(KVariance.OUT, String::class.starProjectedType),
))) {
    override fun decodeFrom(value: String): Map<String, String>? = when {
        value.trimStart().startsWith("{") -> decodeStandardStringStringMapSyntax(value.trim())
        else -> null
    }

    @Suppress("UNCHECKED_CAST")
    override fun convertFrom(value: Any): Map<String, String>? = when (value) {
        is Map<*, *> -> value.map { (key, value) -> Pair(key.toString(), value.toString()) }.toMap()
        is ObjectNode -> convertMapFromObjectNode(value)
        is ArrayNode -> convertMapFromArrayNode(value)
        is Collection<*> -> when {
            value.isEmpty() -> mapOf()
            value.first() is Pair<*, *> ->
                (value as List<Pair<*, *>>).associate { (key, value) -> Pair(key.toString(), value.toString()) }
            else -> throw TypeCastException("Element type is not a Pair!")
        }
        else -> null
    }

    private fun convertMapFromObjectNode(value: ObjectNode): Map<String, String> = value.map { k, v -> Pair(k, v.text) }.toMap()
    private fun convertMapFromArrayNode(value: ArrayNode): Map<String, String> = value.mapNotNull { element ->
        if (element is ObjectNode)
            element.fields().let { it -> if (it.hasNext()) it.next().let { Pair(it.key, it.value.text) } else null }
        else null
    }.toMap()

    private fun decodeStandardStringStringMapSyntax(input: String): Map<String, String> {
        if (input.last() != '}') throw IllegalArgumentException("Standard map representation must end with '}'")
        val x = input.drop(1).dropLast(1)
        var quote = '\u0000'
        var escaped = false
        val result = mutableMapOf<String, String>()
        val key = StringBuilder()
        val value = StringBuilder()
        var current = key
        var hexcape: MutableList<Char>? = null
        fun ship() {
            result += Pair(key.toString(), value.toString())
            key.setLength(0)
            value.setLength(0)
            current = key
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
                c in ":=" -> if (current == key) current = value else throw IllegalStateException("‹$c› out of phase in value part!")
                c in ",;" -> if (current == value) ship() else throw IllegalStateException("‹$c› out of phase in key part: no value!")
                else -> current += c
            }
            if (quote != '\u0000') throw IllegalStateException("Open quote!")
            if (escaped) throw IllegalStateException("Open escape sequence!")
            if (current != key) throw IllegalArgumentException("Missing value!")
        }
        return result.toMap()
    }

}