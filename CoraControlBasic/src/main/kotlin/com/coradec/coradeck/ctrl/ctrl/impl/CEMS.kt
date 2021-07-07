/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.EMS
import com.coradec.coradeck.text.model.LocalText
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit.SECONDS

object CEMS: Logger(), EMS {
    private val TEXT_INVALID_OBJECT_TYPE = LocalText("InvalidObjectType")
    private val TEXT_BROADCAST_NOT_IMPLEMENTED = LocalText("BroadcastNotImplemented")
    private val PROP_QUEUE_SIZE = LocalProperty("QueueSize", 4096)
    private val PROP_LOW_WATER_MARK = LocalProperty("LowWaterMark", 3)
    private val PROP_HIGH_WATER_MARK = LocalProperty("HighWaterMark", 12)
    private val PROP_PATIENCE = LocalProperty("Patience", Timespan(2, SECONDS))

    private val queue = LinkedBlockingDeque<Any>(PROP_QUEUE_SIZE.value)
    private val queueEmptyTriggers = ConcurrentLinkedQueue<() -> Unit>()
    private var enabled = true

    private val MP_ID_GEN = BitSet(999)
    private val NEXT_ID: Int get() = MP_ID_GEN.nextClearBit(0).also { MP_ID_GEN.set(it) }
    private val workers = ConcurrentHashMap<Int, Worker>()

    init {
        startInitialWorkers()
    }

    private fun startInitialWorkers() {
        for (i in 1 .. PROP_LOW_WATER_MARK.value) startWorker()
    }

    private fun startWorker() {
        synchronized(workers) {
            val id = NEXT_ID
            workers[id] = Worker(id).apply { start() }
        }
    }

    override fun execute(agent: Agent) {
        queue.put(agent)
    }

    override fun inject(message: Information) {
        if (message.urgent) {
            queue.putFirst(message)
        } else {
            queue.putLast(message)
        }
    }

    override fun post(obj: Any) {
        queue.put(obj)
    }

    override fun onQueueEmpty(function: () -> Unit) {
        queueEmptyTriggers += function
    }

    override fun standBy() {
        val sync = Semaphore(1)
        onQueueEmpty { sync.release() }
        sync.acquire()
    }

    private class Worker(val number: Int): Thread("CEMS-%03d".format(number)) {
        override fun run() {
            val patience = PROP_PATIENCE.value
            debug("Worker %d starting, patience = %s", number, patience.representation)
            while (!interrupted()) {
                when (val item = queue.poll(patience.amount, patience.unit)) {
                    null -> if (PROP_LOW_WATER_MARK.value < workers.size) break
                    is Information -> broadcast(item)
                    is Agent -> item.trigger()
                    else -> error(TEXT_INVALID_OBJECT_TYPE, item::class.java, item)
                }
            }
            MP_ID_GEN.clear(number)
            workers -= number
            debug("Worker %d stopped.", number)
        }
    }

    private fun broadcast(item: Information) {
        warn(TEXT_BROADCAST_NOT_IMPLEMENTED, item.formatted)
    }
}
