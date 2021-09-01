/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import com.coradec.coradeck.core.model.Null

val Any?.formatted: String
    get() {
        return when (this) {
            is Map<*, *> -> entries.joinToString(", ", "[", "]") { (key, value) -> "$key: ${value.formatted}" }
            is String -> "\"${toString()}\""
            null -> "«null»"
            is Null -> "«null»"
            else -> toString()
        }
    }

operator fun Regex.contains(x: String): Boolean = x.matches(this)
fun String.trimIfBlank() = ifBlank { "" }
operator fun StringBuilder.plusAssign(c: Char) {
    append(c)
}
