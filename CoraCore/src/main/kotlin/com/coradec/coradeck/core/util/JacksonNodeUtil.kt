/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URL
import java.time.LocalDate

val objectMapper = jacksonObjectMapper()

fun <T> ArrayNode.map(mapping: (JsonNode) -> T): Sequence<T> = this.elements().asSequence().map(mapping)
fun <T> ObjectNode.map(mapfn: (key: String, value: JsonNode) -> T): List<T> = mutableListOf<T>().also {
    fields().forEach { (key, value) -> it += mapfn.invoke(key, value) }
}

val String.json: JsonNode get() = objectMapper.readTree(this)
val URL.json: JsonNode get() = objectMapper.readTree(this)
fun JsonNode?.asObjectNode(): ObjectNode = when(this) {
    null -> throw IllegalStateException("Node not found!")
    is ObjectNode -> this
    else -> throw IllegalArgumentException("Node is not an object node!")
}
fun JsonNode?.asListNode(): ArrayNode = when(this) {
    null -> throw IllegalStateException("Node not found!")
    is ArrayNode -> this
    else -> throw IllegalArgumentException("Node is not an array node!")
}
fun JsonNode?.asTextNode(): TextNode = when(this) {
    null -> throw IllegalStateException("Node not found!")
    is TextNode -> this
    else -> throw IllegalArgumentException("Node is not a text node!")
}
val JsonNode?.text: String get() = when(this) {
    null -> throw IllegalStateException("Node not found!")
    is TextNode -> textValue()
    else -> toPrettyString()
}
