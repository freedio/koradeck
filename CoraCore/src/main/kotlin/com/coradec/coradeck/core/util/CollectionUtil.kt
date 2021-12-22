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

fun <T, F : Comparable<F>> Sequence<T>.zipWith(other: Sequence<T>, matchExpr: (T) -> F): Sequence<Pair<T, T>> =
    Sequence { Zipper(sortedBy(matchExpr).iterator(), other.sortedBy(matchExpr).iterator(), matchExpr) }

@Suppress("ComplexRedundantLet")
private class Zipper<T, F : Comparable<F>>(
    private val one: Iterator<T>,
    private val two: Iterator<T>,
    private val matchExpr: (T) -> F
) : Iterator<Pair<T, T>> {
    private var nx1: T? = null
    private var nx2: T? = null
    private val next1: T? get() = nx1 ?: (if (one.hasNext()) one.next() else null).also { nx1 = it }
    private val next2: T? get() = nx2 ?: (if (two.hasNext()) two.next() else null).also { nx2 = it }
    private val nexxt1: T? get() = (if (one.hasNext()) one.next() else null).also { nx1 = it }
    private val nexxt2: T? get() = (if (two.hasNext()) two.next() else null).also { nx2 = it }

    override fun hasNext(): Boolean = when {
        next1 == null || next2 == null -> false
        matchExpr.invoke(next1!!) < matchExpr.invoke(next2!!) -> nexxt1.let { hasNext() }
        matchExpr.invoke(next1!!) > matchExpr.invoke(next2!!) -> nexxt2.let { hasNext() }
        else -> true
    }

    override fun next(): Pair<T, T> = if (hasNext()) Pair(nx1!!, nx2!!).also {
        nx1 = null
        nx2 = null
    } else throw NoSuchElementException()
}
