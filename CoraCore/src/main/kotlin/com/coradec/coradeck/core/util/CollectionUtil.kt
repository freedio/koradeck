package com.coradec.coradeck.core.util

fun <T> Collection<T>.without(predicate: (T) -> Boolean): Collection<T> = when (this) {
    is Set<T> -> HashSet<T>(this).apply { removeIf(predicate) }
    else -> ArrayList<T>(this).apply { removeIf(predicate) }
}