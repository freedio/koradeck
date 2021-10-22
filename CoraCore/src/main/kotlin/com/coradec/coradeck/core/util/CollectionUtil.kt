/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

fun <T> Collection<T>.without(predicate: (T) -> Boolean): Collection<T> = when (this) {
    is Set<T> -> HashSet<T>(this).apply { removeIf(predicate) }
    else -> ArrayList<T>(this).apply { removeIf(predicate) }
}
@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K?, V>.removeNullKeys(): Map<K, V> = filterKeys { it != null } as Map<K, V>

fun <E> sequenceOf(hasNext: () -> Boolean, next: () -> E): Sequence<E> = object : Iterator<E> {
    override fun hasNext(): Boolean = hasNext.invoke()
    override fun next(): E = next.invoke()
}.asSequence()
