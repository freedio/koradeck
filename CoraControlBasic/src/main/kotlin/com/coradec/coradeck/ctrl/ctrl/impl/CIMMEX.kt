/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.com.model.impl.BasicEvent
import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.ctrl.IMMEX
import com.coradec.coradeck.text.model.LocalText
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.reflect.KClass

/** Central information, market, messaging, event-handling and execution service. */
object CIMMEX : Logger(), IMMEX, Recipient {
    private val TEXT_INVALID_OBJECT_TYPE = LocalText("InvalidObjectType2")
    private val TEXT_NOT_EXECUTABLE = LocalText("NoExecutable2")
    private val TEXT_EXECUTION_ABORTED = LocalText("ExecutionAborted1")
    private val TEXT_NOT_FINISHED = LocalText("NotFinished4")
    private val TEXT_DISPATCH_FAILED = LocalText("DispatchFailed1")
    private val TEXT_CANT_DISPATCH = LocalText("CantDispatch1")
    private val TEXT_MESSAGE_NOT_UNDERSTOOD = LocalText("MessageNotUnderstood2")
    private val TEXT_NO_RECIPIENTS = LocalText("NoRecipients2")
    private val TEXT_SHUTTING_DOWN = LocalText("ShuttingDown")
    private val TEXT_SHUT_DOWN = LocalText("ShutDown")
    private val PROP_INQUEUE_SIZE = LocalProperty("InQueueSize", 4000)
    private val PROP_TASKQUEUE_SIZE = LocalProperty("TaskQueueSize", 1000)
    private val PROP_MIN_WORKERS = LocalProperty("MinWorkers", 3)
    private val PROP_MAX_WORKERS = LocalProperty("MaxWorkers", 12)
    private val PROP_MIN_EXECUTORS = LocalProperty("MinExecutors", 3)
    private val PROP_MAX_EXECUTORS = LocalProperty("MaxExecutors", 12)
    private val PROP_PATIENCE = LocalProperty("Patience", Timespan(2, SECONDS))
    private val PROP_SHUTDOWN_ALLOWANCE = LocalProperty("ShutdownAllowance", Timespan(10, SECONDS))

    private val dispatcher = Dispatcher()
    private val inqueue = LinkedBlockingDeque<Any>(PROP_INQUEUE_SIZE.value)
    private val taskqueue = LinkedBlockingQueue<Any>(PROP_TASKQUEUE_SIZE.value)
    private val executors = ConcurrentHashMap<Int, Executor>()
    private val workers = ConcurrentHashMap<Int, Worker>()
    private val dispatchTable = LinkedHashMap<Recipient, BlockingQueue<Information>>()
    private val dispatchOrder = LinkedBlockingQueue<Recipient>()
    private val undeliveredCount get() = dispatchTable.values.sumOf { it.size }
    private val registry = ConcurrentHashMap<KClass<*>, MutableSet<Recipient>>()

    private val finishTriggers = HashSet<Observer>()
    private var enabled = true
    private val finished: Boolean get() = !enabled && inqueue.isEmpty() && taskqueue.isEmpty() && dispatchTable.isEmpty()

    override val capacity: Int = 16

    private val WORKER_ID_GEN = BitSet(999)
    private val EXCTOR_ID_GEN = BitSet(999)
    private val NEXT_WORKER: Int get() = WORKER_ID_GEN.nextClearBit(0).also { WORKER_ID_GEN.set(it) }
    private val NEXT_EXCTOR: Int get() = EXCTOR_ID_GEN.nextClearBit(0).also { EXCTOR_ID_GEN.set(it) }

    init {
        dispatcher.start()
        for (i in 1..PROP_MIN_WORKERS.value) startWorker()
        Runtime.getRuntime().addShutdownHook(Thread(::shutdown, "T-850"))
    }

    private fun shutdown() {
        info(TEXT_SHUTTING_DOWN)
        val shutdownAllowance: Timespan = PROP_SHUTDOWN_ALLOWANCE.value
        val end = System.nanoTime() + shutdownAllowance.let { it.unit.toNanos(it.amount) }
        enabled = false
        while (!finished && System.nanoTime() < end) cleanout()
        if (!finished)
            warn(TEXT_NOT_FINISHED, shutdownAllowance.representation, inqueue.size, taskqueue.size, undeliveredCount)
        val shutdownEvent = ShutdownCompleteEvent(here)
        finishTriggers.forEach { observer -> observer.notify(shutdownEvent) }
        info(TEXT_SHUT_DOWN)
    }

    private fun cleanout() {
        synchronized(dispatcher) {
            val emptyRecipients = ArrayList<Recipient>()
            dispatchTable.forEach { (recipient, rqueue) -> if (rqueue.isEmpty()) emptyRecipients += recipient }
            emptyRecipients.forEach { dispatchTable.remove(it) }
        }
    }

    private fun startWorker() {
        val id = NEXT_WORKER
        Worker(id).start()
    }

    private fun addWorker() {
        if (enabled && inqueue.size > workers.size && workers.size < PROP_MAX_WORKERS.value) startWorker()
    }

    private fun startExecutor() {
        val id = NEXT_EXCTOR
        Executor(id).start()
    }

    private fun addExecutor() {
        if (enabled && taskqueue.size > executors.size && workers.size < PROP_MAX_EXECUTORS.value) startExecutor()
    }

    override fun execute(task: Runnable) {
        taskqueue.put(task)
        addExecutor()
    }

    override fun <I : Information> inject(message: I): I = message.also {
        if (it.urgent) inqueue.putFirst(it) else inqueue.putLast(it)
        it.enqueue()
        addWorker()
    }

    override fun synchronize() {
        val sync = Semaphore(0)
        inject(Synchronization(sync))
        sync.acquire()
    }

    override fun plugin(klass: KClass<out Information>, vararg listener: Recipient) {
        registry.computeIfAbsent(klass) { CopyOnWriteArraySet() }.addAll(listener)
    }

    override fun unplug(vararg listener: Recipient) {
        registry.forEach { it.value.removeAll(listener) }
    }

    override fun onMessage(message: Information) = when (message) {
        is Synchronization -> message.execute()
        else -> error(TEXT_MESSAGE_NOT_UNDERSTOOD, message.classname, message)
    }

    private class Dispatcher : Thread("Dispatch") {
        override fun run() {
            val patience = PROP_PATIENCE.value
            while (!interrupted() && enabled) {
                val item = inqueue.poll(patience.amount, patience.unit)
                try {
                    when (item) {
                        null -> relax()
                        is Runnable -> taskqueue.offer(item) || inqueue.offer(item) || cantDispatch(item)
                        is Message -> {
                            val recipient = item.recipient
                            if (recipient == null) broadcast(item) else dispatch(recipient, item)
                        }
                        is Information -> broadcast(item)
                        else -> error(TEXT_INVALID_OBJECT_TYPE, item::class.java, item)
                    }
                } catch (e: Exception) {
                    error(TEXT_DISPATCH_FAILED, item!!)
                }
            }
        }
    }

    private fun dispatch(recipient: Recipient, item: Information) = synchronized(dispatcher) {
        var added = false
        val queue = dispatchTable.computeIfAbsent(recipient) {
            added = true
            LinkedBlockingQueue(recipient.capacity)
        }
        if (queue.offer(item) || inqueue.offer(item)) item.dispatch() else cantDispatch(item)
        if (added) dispatchOrder.put(recipient)
    }

    private fun broadcast(item: Information) = registry
        .filterKeys { type -> type.isInstance(item) }
        .map { it.value }
        .flatten()
        .distinct()
        .ifEmpty {
            warn(TEXT_NO_RECIPIENTS, item.classname, item)
            item.miss()
            emptyList()
        }
        .forEach { dispatch(it, item) }

    private fun cantDispatch(item: Any): Boolean = false.also {
        error(TEXT_CANT_DISPATCH, item)
    }

    private class Executor(val id: Int) : Thread("Exec-%03d".format(id)) {
        override fun run() {
            executors[id] = this
            val patience = PROP_PATIENCE.value
            debug("Executor %d starting, patience = %s", id, patience.representation)
            while (!interrupted() && enabled) {
                val item = taskqueue.poll(patience.amount, patience.unit)
                try {
                    when (item) {
                        null -> if (executors.size > PROP_MIN_EXECUTORS.value) break
                        is Runnable -> item.run()
                        else -> error(TEXT_NOT_EXECUTABLE, item.classname, item)
                    }
                } catch (e: Exception) {
                    error(TEXT_EXECUTION_ABORTED, item!!)
                }
            }
            EXCTOR_ID_GEN.clear(id)
            executors -= id
            debug("Executor %d stopped.", id)
        }
    }

    private class Worker(val id: Int) : Thread("Work-%03d".format(id)) {
        override fun run() {
            workers[id] = this
            val patience = PROP_PATIENCE.value
            debug("Worker %d starting, patience = %s", id, patience.representation)
            while (!interrupted() && enabled) {
                val recipient = dispatchOrder.poll(patience.amount, patience.unit)
                if (recipient != null) process(recipient)
                else if (workers.size > PROP_MIN_WORKERS.value) break
            }
            WORKER_ID_GEN.clear(id)
            workers -= id
            debug("Worker %d stopped.", id)
        }

        private fun process(recipient: Recipient) {
            val item: Information? = synchronized(dispatcher) {
                dispatchTable[recipient]?.poll().also { if (it == null) dispatchTable -= recipient }
            }
            if (item != null) {
                try {
                    item.delivered()
                    recipient.onMessage(item)
                    item.processed()
                } catch (e: Exception) {
                    error(TEXT_EXECUTION_ABORTED, item)
                    if (item is Request) item.fail(e)
                }
                dispatchOrder.put(recipient)
            }
        }
    }

    private class Synchronization(val sync: Semaphore, target: Recipient? = this) : BasicCommand(here, target = target) {
        override val copy: BasicCommand get() = Synchronization(sync, recipient)
        override fun copy(recipient: Recipient): BasicCommand = Synchronization(sync, recipient)

        override fun execute() {
            sync.release()
            succeed()
        }
    }

    private class ShutdownCompleteEvent(origin: Origin) : BasicEvent(origin)
}
