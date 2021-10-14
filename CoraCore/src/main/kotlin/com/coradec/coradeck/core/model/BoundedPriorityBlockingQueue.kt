/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

class BoundedPriorityBlockingQueue<T>(capacity: Int) : PriorityBlockingQueue<T>() {
    private val cover = capacity - 1
    private val semaphore = Semaphore(1)

    override fun poll(): T? = super.poll().also { if (it != null && size == cover) semaphore.release() }
    override fun poll(timeout: Long, unit: TimeUnit): T? = 
        super.poll(timeout, unit).also { if (it != null && size == cover) semaphore.release() }
    override fun take(): T = super.take().also { if (size == cover) semaphore.release() }
    override fun offer(e: T): Boolean = offer(e, Long.MAX_VALUE, TimeUnit.DAYS)
    override fun offer(e: T, timeout: Long, unit: TimeUnit): Boolean = semaphore.tryAcquire(timeout, unit).also {
        if (it) try {
            super.offer(e)
        } finally {
            if (size <= cover) semaphore.release()
        }
    }
}
