/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.util.*
import kotlin.NoSuchElementException

class CacheQueue<E> : Queue<E> {
    val softcache = LinkedList<SoftReference<E>>()
    private val refQ = ReferenceQueue<E>()
    override val size: Int get() = cleanup().let { softcache.size }

    private fun cleanup() {
        do {
            val ref = refQ.poll()?.run { softcache.remove(this) }
        } while (ref != null)
    }

    private fun Int.wrap(size: Int): Int = if (this < 0) size + this else this

    override fun add(element: E): Boolean = cleanup().let { softcache.add(SoftReference(element, refQ)) }
    fun add(index: Int, element: E) = cleanup().let { softcache.add(index.wrap(size), SoftReference(element, refQ)) }
    override fun contains(element: E): Boolean = softcache.any { it.get() == element }
    override fun containsAll(elements: Collection<E>): Boolean = cleanup().let { softcache.mapNotNull { it.get() }.containsAll(elements) }
    override fun isEmpty(): Boolean = cleanup().let { softcache.isEmpty() }
    override fun addAll(elements: Collection<E>): Boolean = cleanup().let { softcache.addAll(elements.map { SoftReference(it, refQ) }) }
    override fun clear() = softcache.clear()
    override fun iterator(): MutableIterator<E> = CacheQueueIterator()
    override fun remove(): E = softcache.remove()?.get() ?: if (isNotEmpty()) remove() else throw NoSuchElementException()
    override fun removeAll(elements: Collection<E>): Boolean = cleanup().let { softcache.removeIf { it.get() in elements } }
    override fun retainAll(elements: Collection<E>): Boolean = cleanup().let { softcache.removeIf { it.get() !in elements } }
    override fun offer(element: E): Boolean = cleanup().let { softcache.offer(SoftReference(element, refQ)) }
    override fun poll(): E? = softcache.poll()?.get() ?: if (isNotEmpty()) poll() else null
    override fun element(): E = peek() ?: throw NoSuchElementException()
    override fun peek(): E? = cleanup().let { softcache.peek()?.get() }
    override fun remove(element: E): Boolean {
        cleanup()
        val it = iterator()
        while (it.hasNext()) {
            if (it.next() == element) {
                it.remove()
                return true
            }
        }
        return false
    }

    private inner class CacheQueueIterator : MutableIterator<E> {
        val softit = softcache.iterator()

        override fun hasNext(): Boolean = softit.hasNext()
        override fun next(): E {
            var next: E?
            do {
                next = softit.next().get()
            } while (next == null && hasNext())
            if (next == null) throw NoSuchElementException()
            return next
        }

        override fun remove() = softit.remove()
    }

}
