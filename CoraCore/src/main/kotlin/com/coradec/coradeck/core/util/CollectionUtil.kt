package com.coradec.coradeck.core.util

fun <T> Collection<T>.without(predicate: (T) -> Boolean): Collection<T> = when (this) {
    is Set<T> -> HashSet<T>(this).apply { removeIf(predicate) }
    else -> ArrayList<T>(this).apply { removeIf(predicate) }
}
@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K?, V>.removeNullKeys(): Map<K, V> = filterKeys { it != null } as Map<K, V>
