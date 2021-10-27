/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import com.coradec.coradeck.core.model.Formattable
import com.coradec.coradeck.core.model.Null
import com.coradec.coradeck.core.trouble.BasicException

val Any?.formatted: String get() = formatAnyOrNull(this, emptySet<Any>(), this)
fun Any?.formatted(known: Set<Any?> = emptySet<Any>()) = formatAnyOrNull(this, known, this)
private fun formatAnyOrNull(obj: Any?, known: Set<Any?>, new: Any?): String = when (obj) {
    null -> "‹null›"
    is Null -> "‹null›"
    in known -> "‹known (see above)›"
    is Map<*, *> -> obj.entries.joinToString(", ", "[", "]") { (key, value) -> "$key: ${formatAnyOrNull(value, known + new, obj)}" }
    is String -> "\"$obj\""
    is BasicException -> "${obj.classname}: ${obj.message(known)}"
    is Formattable -> obj.format(known)
    else -> obj.toString()
}

operator fun Regex.contains(x: String): Boolean = x.matches(this)
fun String.trimIfBlank() = ifBlank { "" }
operator fun StringBuilder.plusAssign(c: Char) {
    append(c)
}
