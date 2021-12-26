/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.com.model.impl.BasicEvent
import com.coradec.coradeck.com.model.impl.BasicNotification
import com.coradec.coradeck.com.trouble.NotificationRejectedException
import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.core.model.*
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.trouble.StandbyTimeoutException
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.IMMEX
import com.coradec.coradeck.ctrl.model.PrioQueue
import com.coradec.coradeck.ctrl.model.Task
import com.coradec.coradeck.ctrl.module.CoraControl.createRequestList
import com.coradec.coradeck.ctrl.trouble.NotificationAlreadyEnqueuedException
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.io.PrintWriter
import java.io.StringWriter
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

/** Central information, market, messaging, event-handling and execution service. */
object CIMMEX : Logger(), IMMEX {
    private val TEXT_INVALID_OBJECT_TYPE = LocalText("InvalidObjectType2")
    private val TEXT_EXECUTION_ABORTED = LocalText("ExecutionAborted1")
    private val TEXT_NOT_FINISHED = LocalText("NotFinished5")
    private val TEXT_DISPATCH_FAILED = LocalText("DispatchFailed1")
    private val TEXT_CANT_DISPATCH = LocalText("CantDispatch1")
    private val TEXT_NO_RECIPIENTS = LocalText("NoRecipients2")
    private val TEXT_SHUTTING_DOWN = LocalText("ShuttingDown")
    private val TEXT_SHUT_DOWN = LocalText("ShutDown")
    private val TEXT_TIMER_STARTED = LocalText("TimerStarted")
    private val TEXT_TIMER_STOPPED = LocalText("TimerStopped")
    private val TEXT_TIMER_CRASHED = LocalText("TimerCrashed")
    private val TEXT_DISPATCHER_STARTED = LocalText("DispatcherStarted")
    private val TEXT_DISPATCHER_STOPPED = LocalText("DispatcherStopped")
    private val TEXT_DISPATCHER_CRASHED = LocalText("DispatcherCrashed")
    private val TEXT_DISPATCHER_INTERRUPTED = LocalText("DispatcherInterrupted")
    private val TEXT_CIMMEX_DISABLED = LocalText("CimmexDisabled1")
    private val TEXT_REMAINING_ITEMS = LocalText("RemainingItems1")
    private val TEXT_WAITING_FOR_SHUTDOWN_CLEARANCE = LocalText("WaitingForShutdownClearance")
    private val TEXT_SHUTDOWN_CLEARANCE_ACQUIRED = LocalText("ShutdownClearanceAcquired")
    private val TEXT_SHUTDOWN_CLEARANCE_DENIED = LocalText("ShutdownClearanceDenied1")
    private val PROP_INQUEUE_SIZE = LocalProperty("InQueueSize", 4000)
    private val PROP_TASKQUEUE_SIZE = LocalProperty("TaskQueueSize", 1000)
    private val PROP_DEFERRINGQUEUE_SIZE = LocalProperty("DeferringQueueSize", 1000)
    private val PROP_BROADCAST_QUEUE_SIZE = LocalProperty("BroadcastQueueSize", 40000)
    private val PROP_MAX_RECIPIENTS = LocalProperty("MaxRecipients", 1000)
    private val PROP_MIN_WORKERS = LocalProperty("MinWorkers", 3)
    private val PROP_MAX_WORKERS = LocalProperty("MaxWorkers", 12)
    private val PROP_MIN_EXECUTORS = LocalProperty("MinExecutors", 3)
    private val PROP_MAX_EXECUTORS = LocalProperty("MaxExecutors", 12)
    private val PROP_PATIENCE = LocalProperty("Patience", Timespan(2, SECONDS))
    private val PROP_SHUTDOWN_ALLOWANCE = LocalProperty("ShutdownAllowance", Timespan(10, SECONDS))

    private val dispatcher = Dispatcher()
    private val timer = Timer()
    private val inqueue = PrioQueue<Prioritized>(PROP_INQUEUE_SIZE.value)
    private val taskqueue = PrioQueue<Task>(PROP_TASKQUEUE_SIZE.value)
    private val delayQueue = DeferringQueue()
    private val broadcastQueue = PrioQueue<Notification<*>>(PROP_BROADCAST_QUEUE_SIZE.value)
    private val lostMessages = ConcurrentLinkedQueue<Notification<*>>()
    private val workerGroup = WorkerThreadGroup()
    private val executors = ConcurrentHashMap<Int, Executor>()
    private val workers = ConcurrentHashMap<Int, Worker>()
    private val defaultAgent = DefaultAgent()
    private val dispatchTable = ConcurrentHashMap<Recipient, PrioQueue<Notification<*>>>()
    private val dispatchOrder = PrioQueue<Recipient>(PROP_MAX_RECIPIENTS.value)
    private val dispatchBlock = CopyOnWriteArraySet<Recipient>()
    private val undeliveredCount get() = dispatchTable.values.sumOf { it.size }
    private val registry = LinkedList<Recipient>()
    private val finishTriggers = HashSet<Observer>()
    private var enabled = AtomicBoolean(true)
    override val load: Int get() = inqueue.size + taskqueue.size + delayQueue.size + undeliveredCount
    private val clear: Boolean get() = load == 0
    override val stats: String
        get() = "Enabled: %s, InQueue: %d, TaskQueue: %d, DelayQueue: %d, Undelivered: %d".format(
            if (enabled.get()) "Yes" else "No", inqueue.size, taskqueue.size, delayQueue.size, undeliveredCount
        )
    private val shutdownSemaphore = Semaphore(1)
    private val shutdownClearance = AtomicInteger(1)
    private val WORKER_ID_GEN = BitSet(999)
    private val EXCTOR_ID_GEN = BitSet(999)
    private val NEXT_WORKER: Int get() = WORKER_ID_GEN.nextClearBit(0).also { WORKER_ID_GEN.set(it) } + 1
    private val NEXT_EXCTOR: Int get() = EXCTOR_ID_GEN.nextClearBit(0).also { EXCTOR_ID_GEN.set(it) } + 1

    init {
        dispatcher.start()
        timer.start()
        for (i in 1..PROP_MIN_WORKERS.value) startWorker()
        Runtime.getRuntime().addShutdownHook(Thread(::shutdown, "T-850"))
    }

    private fun shutdown() {
        val shutdownAllowance: Timespan = PROP_SHUTDOWN_ALLOWANCE.value
        val end = System.nanoTime() + shutdownAllowance.let { it.unit.toNanos(it.amount) }
        info(TEXT_WAITING_FOR_SHUTDOWN_CLEARANCE)
        if (!shutdownSemaphore.tryAcquire(shutdownClearance.get(), shutdownAllowance.amount, shutdownAllowance.unit))
            error(TEXT_SHUTDOWN_CLEARANCE_DENIED, shutdownAllowance)
        else info(TEXT_SHUTDOWN_CLEARANCE_ACQUIRED)
        info(TEXT_SHUTTING_DOWN)
        enabled.set(false)
        timer.interrupt()
        dispatcher.interrupt()
        while (!clear && System.nanoTime() < end) cleanout()
        if (!clear)
            warn(TEXT_NOT_FINISHED, shutdownAllowance.representation, inqueue.size, taskqueue.size, undeliveredCount, dispatchTable
                .flatMap { (key, value) -> value.asSequence().map { Pair(key, it.content) } }
                .joinToString("\n", "\n") { "${it.first} ← ${it.second}" }
            )
        val shutdownEvent = ShutdownCompleteEvent(here)
        finishTriggers.forEach { observer -> observer.notify(shutdownEvent) }
        info(TEXT_SHUT_DOWN)
    }

    private fun cleanout() {
        synchronized(dispatcher) {
            dispatchTable.filterValues { it.isEmpty() }.keys.forEach { dispatchTable.remove(it) }
        }
        Thread.yield()
    }

    private fun startWorker() {
        val id = NEXT_WORKER
        Worker(id).start()
    }

    private fun addWorker() {
        if (enabled.get() && undeliveredCount >= workers.size && workers.size < PROP_MAX_WORKERS.value) startWorker()
    }

    private fun startExecutor() {
        val id = NEXT_EXCTOR
        Executor(id).start()
    }

    private fun addExecutor() {
        if (enabled.get() && taskqueue.size >= executors.size && executors.size < PROP_MAX_EXECUTORS.value) startExecutor()
    }

    override fun execute(executable: Runnable) = execute(executable, defaultPriority)
    override fun execute(executable: Runnable, prio: Priority) = execute(Task(executable, prio))
    override fun execute(task: Task) {
        if (task.deferred) delayQueue.put(task) else taskqueue.put(task)
        addExecutor()
    }

    override fun <I : Information> inject(info: I): Notification<I> = Notification(info).also {
        ifEnabled(it) {
            if (info.deferred) delayQueue.put(it) else inqueue.put(it)
            it.enqueue()
            addWorker()
        }
    }

    override fun <I : Information, N : Notification<I>> inject(notification: N): N = notification.also {
        ifEnabled(it) {
            if (notification.enqueued) throw NotificationAlreadyEnqueuedException(notification, notification.states)
            if (notification.deferred)
                delayQueue.put(it)
            else
                inqueue.put(it)
            notification.enqueue()
            addWorker()
        }
    }

    private fun inject(origin: Origin, recipient: Recipient, function: () -> Unit) {
        inject(BasicNotification(MacroCommand(origin, function), origin) unto recipient)
    }

    override fun synchronize() {
        if (enabled.get()) {
            val sync = Semaphore(0)
            defaultAgent.accept(Synchronization(sync))
            cleanBroadcastQueue()
            sync.acquire()
        } else error(TEXT_CIMMEX_DISABLED, "Synchronization")
    }

    override fun standby() = standby(PROP_SHUTDOWN_ALLOWANCE.value)
    override fun standby(delay: Timespan) {
        val end = System.nanoTime() + delay.let { it.unit.toNanos(it.amount) }
        while (!clear && System.nanoTime() < end) cleanout()
        if (!clear) {
            warn(TEXT_REMAINING_ITEMS, fullStats())
            throw StandbyTimeoutException(delay)
        }
        Thread.sleep(10)
    }

    override fun preventShutdown() {
        shutdownClearance.incrementAndGet()
    }

    override fun allowShutdown() {
        shutdownSemaphore.release()
    }

    override fun subscribe(vararg listeners: Recipient) {
        synchronized(registry) { registry += listeners.toSet() }
        cleanBroadcastQueue()
        broadcastQueue.forEach { notification ->
            val information = notification.content
            listeners.forEach { listener -> if (listener.accepts(information)) dispatch(notification, listener) }
        }
    }

    override fun unsubscribe(vararg listeners: Recipient) {
        defaultAgent.accept(createRequestList(here, listeners.map { listener -> UnsubscribeRequest(here, listener) }, defaultAgent))
    }

    private class UnsubscribeRequest(origin: Origin, val listener: Recipient) : BasicCommand(origin) {
        override fun execute() {
            synchronized(registry) { registry -= listener }
            succeed()
        }
    }

    override fun block(recipient: Recipient): Unit = synchronized(dispatcher) {
        dispatchBlock.add(recipient) && dispatchOrder.remove(recipient)
    }

    override fun release(recipient: Recipient): Unit = synchronized(dispatcher) {
        if (dispatchBlock.remove(recipient) && recipient in dispatchTable.keys) dispatchOrder.put(recipient)
    }

    override fun printFullStats() {
        println(fullStats())
    }

    private fun fullStats(): String {
        cleanBroadcastQueue()
        val collector = StringWriter()
        val undelivered = undeliveredCount
        PrintWriter(collector).use { out ->
            out.println("     Basic Stats: $stats")
            out.println("         Workers: ${workers.size}")
            out.println("         Inqueue: (#${inqueue.size})${if (inqueue.isNotEmpty()) inqueue.joinToString("\n• ", ":\n• ") else ""}")
            out.println("       TaskQueue: (#${taskqueue.size})${if (taskqueue.isNotEmpty()) taskqueue.joinToString("\n• ", ":\n• ") else ""}")
            out.println("      Recipients: (#${dispatchOrder.size})${if (dispatchOrder.isNotEmpty()) dispatchOrder.joinToString("\n• ", ":\n• ", limit = 50) else ""}")
            out.println("   Lost Messages: (#${lostMessages.size})${if (lostMessages.isNotEmpty()) lostMessages.joinToString("\n• ", ":\n• ", limit = 50) else ""}")
            out.println("Dispatcher Table: (#${undelivered})${if (undelivered > 0) dispatchTable.entries
                .filter { it.value.isNotEmpty() }
                .joinToString("\n\n", ": {\n", "\n}", limit = 48) { (recipient, entries) ->
                    "------ $recipient ------${entries.joinToString("\n• ", "\n• ", limit = 64)}"
                }
            else ""}")
            out.println(" Broadcast Queue: (#${broadcastQueue.size}) ${if (broadcastQueue.isNotEmpty()) broadcastQueue.joinToString("\n• ", "\n• ", limit = 50) else ""}")
        }
        return collector.toString()
    }

    private fun ifEnabled(note: Notification<*>, action: () -> Unit) {
        if (enabled.get()) action.invoke()
        else error(TEXT_CIMMEX_DISABLED, note)
    }

    private class Timer : Thread("Timer") {
        override fun run() {
            try {
                info(TEXT_TIMER_STARTED)
                while (enabled.get()) {
                    val sleep = delayQueue.peek()?.delayMs ?: Long.MAX_VALUE
                    trace("Timer sleeping for $sleep ms.")
                    try {
                        sleep(sleep)
                        trace("Timer awoke.")
                        dispatch(delayQueue)
                    } catch (e: InterruptedException) {
                        trace("Timer rescheduled.")
                    }
                }
                info(TEXT_TIMER_STOPPED)
            } catch (e: Throwable) {
                error(e, TEXT_TIMER_CRASHED)
                throw e
            }
        }
    }

    private class Dispatcher : Thread("Dispatch") {
        override fun run() {
            try {
                info(TEXT_DISPATCHER_STARTED)
                while (!interrupted()) {
                    try {
                        dispatch(inqueue)
                    } catch (e: InterruptedException) {
                        if (enabled.get()) warn(TEXT_DISPATCHER_INTERRUPTED)
                    }
                }
                info(TEXT_DISPATCHER_STOPPED)
            } catch (e: Throwable) {
                abort(e, TEXT_DISPATCHER_CRASHED)
                exitProcess(250)
            }
        }
    }

    private fun <T : Any> dispatch(q: PrioQueue<T>) {
        val item = q.take()
        try {
            when (item) {
                is Task -> taskqueue.offer(item) || inqueue.offer(item) || cantDispatch(item)
                is MultiRequest -> try {
                    item.execute()
                    item.succeed()
                } catch (e: Exception) {
                    item.fail(e)
                }
                is Message<*> -> dispatch(item)
                is Notification<*> -> broadcast(item)
                else -> error(TEXT_INVALID_OBJECT_TYPE, item::class.java, item)
            }
        } catch (e: Exception) {
            abort(e, TEXT_DISPATCH_FAILED, item)
            exitProcess(250)
        }
    }

    private fun dispatch(message: Message<*>) = dispatch(message, message.recipient)
    private fun dispatch(information: Information, recipient: Recipient) = dispatch(BasicNotification(information), recipient)
    private fun dispatch(notification: Notification<*>, recipient: Recipient) {
        var action: () -> Unit = { }
        synchronized(dispatcher) {
            val queue = dispatchTable.computeIfAbsent(recipient) {
                PrioQueue(1024)
            }
            val e = queue.isEmpty()
            if (queue.offer(notification)) {
                action = { notification.dispatch() }
                if (e && recipient !in dispatchBlock) dispatchOrder.put(notification.priority, recipient)
            } else if (!inqueue.offer(notification)) action = { cantDispatch(notification) }
        }
        action.invoke()
    }

    private fun broadcast(item: Notification<*>) {
        cleanBroadcastQueue()
        broadcastQueue.add(item)
        val recipients = synchronized(registry) { registry.filter { observer -> observer.accepts(item) } }
        when (recipients.size) {
            0 -> alert(TEXT_NO_RECIPIENTS, item.content.classname, item.content).also { item.discard() }
            1 -> dispatch(item, recipients.single())
            else -> {
                recipients.forEach { dispatch(item.content, it) }
                item.discard()
            }
        }
    }

    private fun cleanBroadcastQueue() {
        val now = ZonedDateTime.now()
        broadcastQueue.filterTo(lostMessages) { it.validUpTo.isBefore(now) && it.lost }
        broadcastQueue.removeIf { it.validUpTo.isBefore(now) }
    }

    private fun cantDispatch(item: Any): Boolean = false.also {
        error(TEXT_CANT_DISPATCH, item)
    }

    private val Recipient.order get() = dispatchTable[this]?.peek()

    private class Executor(val id: Int) : Thread("Exec-%03d".format(id)) {
        override fun run() {
            executors[id] = this
            val patience = PROP_PATIENCE.value
            debug("Executor %d starting, patience = %s", id, patience.representation)
            while (!interrupted() && enabled.get()) {
                val item = taskqueue.poll(patience.amount, patience.unit)
                try {
                    @Suppress("USELESS_IS_CHECK")
                    when (item) {
                        null -> if (executors.size > PROP_MIN_EXECUTORS.value) break
                        is Task -> item.run()
                    }
                } catch (e: Exception) {
                    error(e, TEXT_EXECUTION_ABORTED, item!!)
                }
            }
            EXCTOR_ID_GEN.clear(id)
            executors -= id
            debug("Executor %d stopped.", id)
        }
    }

    private class Worker(val id: Int) : Thread(workerGroup, "Work-%03d".format(id)) {
        override fun run() {
            workers[id] = this
            val patience = PROP_PATIENCE.value
            debug("Worker %d starting, patience = %s", id, patience.representation)
            while (!interrupted() && enabled.get()) {
                val recipient = dispatchOrder.poll(patience.amount, patience.unit)
                if (recipient != null) process(recipient)
                else if (workers.size > PROP_MIN_WORKERS.value) break
            }
            WORKER_ID_GEN.clear(id)
            workers -= id
            debug("Worker %d stopped.", id)
        }

        private fun process(recipient: Recipient) {
            val item: Notification<*>? = try {
                synchronized(dispatcher) {
                    dispatchTable[recipient]?.poll().also { if (it == null) dispatchTable -= recipient }
                }
            } catch (e: Exception) {
                error(e)
                null
            }
            if (item != null) {
                trace(">>> Processing item ‹%s› of recipient ‹%s›.", item.classname, recipient)
                try {
                    Session.override(item.session)
                    if (recipient.accepts(item)) {
                        trace("++> Item accepted.")
                        item.deliver()
                        recipient.receive(item)
                        item.process()
                    } else {
                        trace("−−> Item rejected.")
                        item.discard()
                    }
                    synchronized(dispatcher) {
                        if (recipient !in dispatchBlock) {
                            val next = dispatchTable[recipient]
                            if (next != null && next.isNotEmpty()) dispatchOrder.put(next.first().priority, recipient)
                        }
                    }
                } catch (e: NotificationRejectedException) {
                    item.reject(e)
                } catch (e: Exception) {
                    error(e, TEXT_EXECUTION_ABORTED, item)
                    item.crash(e)
                }
            }
        }
    }

    private class WorkerThreadGroup : ThreadGroup("CIMMEX-Workers")

    internal class Synchronization(private val sync: Semaphore) : BasicCommand(here) {
        override fun execute() {
            sync.release()
            succeed()
        }
    }

    private class RecipientComparator : Comparator<Recipient> {
        override fun compare(o1: Recipient?, o2: Recipient?): Int =
            when {
                o1 == null && o2 == null -> 0
                o1 == null -> 1
                o2 == null -> -1
                else -> {
                    val i1 = o1.order
                    val i2 = o2.order
                    when {
                        i1 == null && i2 == null -> 0
                        i1 == null -> 1
                        i2 == null -> -1
                        i1.priority == i2.priority -> i1.due.compareTo(i2.due)
                        else -> i1.priority.compareTo(i2.priority)
                    }
                }
            }
    }

    private class DeferringQueue : PrioQueue<Deferred>(PROP_DEFERRINGQUEUE_SIZE.value) {
        override fun put(element: Deferred) {
            super.put(element)
            timer.interrupt()
        }

        override fun take(): Deferred {
            return super.take().also { timer.interrupt() }
        }
    }

    private class ShutdownCompleteEvent(origin: Origin) : BasicEvent(origin)

    private class DefaultAgent : BasicAgent() {
        init {
            approve(CIMMEX.Synchronization::class, UnsubscribeRequest::class)
        }

        override fun accepts(notification: Notification<*>) = when (notification.content) {
            is CIMMEX.Synchronization, is UnsubscribeRequest -> true
            else -> super.accepts(notification)
        }
    }
}
