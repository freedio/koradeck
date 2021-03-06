/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import com.coradec.coradeck.core.model.Null

val NEWLINE = System.getProperty("line.separator")
val Any?.formatted: String
    get() {
        val repr = toString()
        return when (this) {
            is String -> "\"$repr\""
            null -> "«null»"
            is Null -> "«null»"
            else -> repr
        }
    }

operator fun Regex.contains(x: String): Boolean = x.matches(this)
fun String.trimIfBlank() = if (isBlank()) "" else this
