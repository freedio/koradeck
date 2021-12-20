/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.core.model.Prioritized
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.util.swallow
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

open class PrioQueue<T>(private val capacity: Int): Logger() {
    private val internalQueues = LinkedHashMap<Priority, LinkedList<T>>()
    private val semaphore = Semaphore(0, true)
    private val capaphore = Semaphore(capacity, true)

    val size: Int get() = semaphore.availablePermits()
    val stats get() =
        "capacity: $capacity, size: ${internalQueues.values.flatten().size}, semaphore: ${semaphore.availablePermits()}, capaphore: ${capaphore.availablePermits()}"

    fun peek(): T? = synchronized(internalQueues) {
        for (prio in Priority.values()) {
            val result = internalQueues[prio]?.peek()
            if (result != null) return result
        }
        return null
    }

    fun poll(amount: Long, unit: TimeUnit): T? {
        var result: T? = null
        if (semaphore.tryAcquire(amount, unit)) synchronized(internalQueues) {
            for (prio in Priority.values()) {
                result = internalQueues[prio]?.poll()
                if (result != null) {
                    capaphore.release()
                    break
                }
            }
        }
        return result
    }

    fun poll(): T? = synchronized(internalQueues) {
        for (prio in Priority.values()) {
            val result = internalQueues[prio]?.poll()
            if (result != null) {
                semaphore.acquireUninterruptibly()
                capaphore.release()
                return result
            }
        }
        return null
    }

    fun put(priority: Priority, element: T) {
        capaphore.acquireUninterruptibly()
        synchronized(internalQueues) {
            internalQueues.computeIfAbsent(priority) { LinkedList() }.add(element).swallow()
        }
        semaphore.release()
    }

    open fun put(element: T) =
        if (element is Prioritized) put(element.priority, element) else throw IllegalArgumentException("Element !is Prioritized!")

    fun add(element: T) {
        if (isFull()) poll()
        put(element)
    }

    fun first(): T = peek() ?: throw NoSuchElementException("first")
    fun isEmpty(): Boolean = size == 0
    fun isNotEmpty(): Boolean = size != 0
    fun isFull(): Boolean = size == capacity
    fun removeIf(filter: (T) -> Boolean) = synchronized(internalQueues) {
        Priority.values().forEach { prio -> internalQueues[prio]?.removeIf(filter) }
        semaphore.drainPermits()
        semaphore.release(internalQueues.values.flatten().size)
        capaphore.drainPermits()
        capaphore.release(capacity-size)
    }

    fun offer(element: T): Boolean =
        if (element is Prioritized) offer(element.priority, element) else throw IllegalArgumentException("Element !is Prioritized!")

    private fun offer(priority: Priority, element: T): Boolean = synchronized(internalQueues) {
        capaphore.tryAcquire() && internalQueues.computeIfAbsent(priority) { LinkedList() }.add(element)
    }.also { if (it) semaphore.release() }

    open fun take(): T {
        semaphore.acquireUninterruptibly()
        synchronized(internalQueues) {
            for (prio in Priority.values()) {
                val result = internalQueues[prio]?.poll()
                if (result != null) {
                    capaphore.release()
                    return result
                }
            }
            throw InternalError("With proper semaphore management, coming here is impossible!!!")
        }
    }

    fun asSequence(): Sequence<T> = Sequence { iterator() }
    fun iterator(): Iterator<T> = QueueIterator()
    fun forEach(function: (T) -> Unit): Unit = synchronized(internalQueues) { internalQueues.values.flatten().forEach(function) }
    override fun toString(): String = joinToString(", ", "[", "]")
    fun joinToString(
        separator: String = ",",
        prefix: String = "",
        suffix: String = "",
        limit: Int = Int.MAX_VALUE,
        truncation: String = "..."
    ): String = synchronized(internalQueues) {
        internalQueues.values.flatten().joinToString(separator, prefix, suffix, limit, truncation)
    }

    fun remove(element: T): Boolean = synchronized(internalQueues) {
        for (prio in Priority.values()) {
            if (internalQueues[prio]?.remove(element) == true) {
                semaphore.acquireUninterruptibly()
                capaphore.release()
                return true
            }
        }
        return false
    }

    private inner class QueueIterator: /*Mutable*/Iterator<T> {
        private val queues = LinkedHashMap<Priority, List<T>>().apply {
            synchronized(internalQueues) {
                for (prio in Priority.values()) {
                    this[prio] = internalQueues[prio]?.let { ArrayList(it) } ?: emptyList()
                }
            }
        }
        private val priorities = Priority.values().iterator()
        private var currentPrio: Priority? = priorities.next()
        private var currentIterator: Iterator<T>? = nextIterator
        @Suppress("RecursivePropertyAccessor")
        private val nextIterator: Iterator<T>? get() {
            currentPrio = if (priorities.hasNext()) priorities.next() else null
            currentIterator = if (currentPrio == null) null
            else queues[currentPrio]?.iterator() ?: nextIterator
            return currentIterator
        }
        override fun hasNext(): Boolean = currentIterator?.hasNext() == true || nextIterator?.hasNext() == true
        override fun next(): T = if (hasNext()) currentIterator!!.next() else throw NoSuchElementException("next")
//        override fun remove() {
//            TODO("Not yet implemented")
//        }
    }
}
