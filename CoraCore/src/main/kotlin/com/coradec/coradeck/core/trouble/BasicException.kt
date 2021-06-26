/*
 * Copyright ⓒ 2017−2020 by Coradec GmbH. All rights reserved.
 */

package com.coradec.coradeck.core.trouble

import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.core.util.pretty
import java.time.Duration

open class BasicException(message: String?, problem: Throwable?) : Exception(message, problem) {
    constructor(message: String?) : this(message, null)
    constructor(problem: Throwable?) : this(null, problem)
    constructor() : this(null, null)

    private val superMessage: String? = super.message
    override val message: String
        get() {
            val properties = listProperties().map { "${it.key}: ${it.value.formatted}"}.joinToString()
            val result = StringBuilder()
            if (superMessage != null) result.append('"').append(superMessage).append("\" ")
            if (properties.isNotEmpty()) result.append('(').append(properties).append(')')
            return result.toString()
        }

    private fun listProperties(): Map<String, Any> = javaClass.methods
            .filterNot { method -> method.name in IRRELEVANT_METHODS }
            .filter { method -> method.name.matches(PROPERTY_METHOD_PREFIX) }
            .filter { method -> method.parameterCount == 0 && !Void.TYPE.isAssignableFrom(method.returnType) }
            .map { method ->
                val name = method.name.replace(PROPERTY_METHOD_PREFIX, "$2")
                val value = method.invoke(this).let {
                    when(it) {
                        is Duration -> it.pretty
                        else -> it
                    }
                }
                Pair(name, value)
            }
            .filter { mapping -> mapping.second != null }
            .sortedBy { mapping -> mapping.first }
            .toMap()

    companion object {
        private val IRRELEVANT_METHODS = setOf("getClass", "getMessage", "getLocalizedMessage", "getStackTrace", "getSuppressed")
        private val PROPERTY_METHOD_PREFIX = Regex("(get|is)(.+)")
    }
}
