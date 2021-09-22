/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.trouble.UnknownJsonNodeTypeException
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.Text
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import java.net.URL

abstract class JacksonConfigurationReader : BasicConfigurationReader() {
    protected abstract val mapper: ObjectMapper

    override fun read(location: URL): Map<String, Any> = mapper.readTree(location).remap(location)

    @Suppress("UNCHECKED_CAST")
    private fun JsonNode?.remap(location: URL): Map<String, Any> = when (this) {
        null -> emptyMap<String, Any>().also { warn(TEXT_CONFIGURATION_ABSENT, location) }
        is ObjectNode -> normalize(location) as Map<String, Any>
        else -> emptyMap<String, Any>().also { warn(TEXT_INVALID_CONFIGURATION, location) }
    }

    private fun ObjectNode.normalize(location: URL): Map<*, *> =
            fields().toMutableMap().mapValues { (_, value) -> nodeValue(value, location) }

    private fun ArrayNode.normalize(location: URL): List<*> =
            elements().toMutableList().map { value -> nodeValue(value, location) }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun nodeValue(value: JsonNode?, location: URL) = when (value) {
        is ArrayNode -> value.normalize(location)
        is ObjectNode -> value.normalize(location)
        null -> null
        is NullNode -> null
        is MissingNode -> null
        is IntNode -> value.intValue()
        is LongNode -> value.longValue()
        is FloatNode -> value.floatValue()
        is DoubleNode -> value.doubleValue()
        is BigIntegerNode -> value.bigIntegerValue()
        is BooleanNode -> value.booleanValue()
        is TextNode -> value.asText()
        is BinaryNode -> value.binaryValue()
        else -> throw UnknownJsonNodeTypeException(value)
    }

    private fun <K, V> MutableIterator<Map.Entry<K, V>>.toMutableMap(): MutableMap<K, V> {
        val result = mutableMapOf<K, V>()
        forEachRemaining {
            result[it.key] = it.value
        }
        return result
    }

    private fun <T> MutableIterator<T>.toMutableList(): MutableList<T> {
        val result = mutableListOf<T>()
        forEachRemaining {
            result += it
        }
        return result
    }

    companion object {
        private val TEXT_INVALID_CONFIGURATION: Text = LocalText("InvalidConfiguration1")
        private val TEXT_CONFIGURATION_ABSENT: Text = LocalText("ConfigurationAbsent1")
    }
}
