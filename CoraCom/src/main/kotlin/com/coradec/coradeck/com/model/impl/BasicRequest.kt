/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.RequestState
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.RequestState.Companion.FINISHED
import com.coradec.coradeck.com.model.RequestStateObserver
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.com.trouble.RequestCancelledException
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.com.trouble.RequestNotAcceptedException
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.trouble.StandbyTimeoutException
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore

open class BasicRequest(
    origin: Origin,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime  = validFrom + CoraCom.standardValidity
) : BasicEvent(origin, priority, createdAt, session, validFrom, validUpto), Request {
    private val unfinished = CountDownLatch(1)
    private var myReason: Throwable? = null
    override val reason: Throwable? get() = myReason
    private val stateRegistry = CopyOnWriteArraySet<Observer>()
    private val myStates = EnumSet.of(NEW)
    override val states: EnumSet<RequestState> get() = EnumSet.copyOf(myStates)
    override val new: Boolean get() = myStates.singleOrNull() == NEW
    override val enqueued: Boolean get() = ENQUEUED in myStates
    override val dispatched: Boolean get() = DISPATCHED in myStates
    override val delivered: Boolean get() = DELIVERED in myStates
    override val processed: Boolean get() = PROCESSED in myStates
    override val lost: Boolean get() = LOST in myStates
    override val successful: Boolean get() = state == SUCCESSFUL
    override val failed: Boolean get() = state == FAILED
    override val cancelled: Boolean get() = state == CANCELLED
    override val complete: Boolean get() = state in FINISHED
    override val observerCount: Int get() = stateRegistry.size
    private val successActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val failureActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val cancellationActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val postActionSemaphore = Semaphore(1)

    override var state: RequestState
        get() = myStates.last()
        set(state) {
            interceptSetState(state)
            var event: RequestStateChangedEvent? = null
            synchronized(myStates) {
                if (state !in myStates) {
                    event = RequestStateChangedEvent(here, this, myStates.last(), state.apply { myStates += this })
                }
            }
            if (event != null) stateRegistry.forEach { if (it.notify(event!!)) stateRegistry.remove(it) }
        }

    protected open fun interceptSetState(state: RequestState) = relax()

    override fun enqueue() {
        state = ENQUEUED
    }

    override fun dispatch() {
        state = DISPATCHED
    }

    override fun deliver() {
        state = DELIVERED
    }

    override fun process() {
        state = PROCESSED
    }

    override fun discard() {
        state = LOST
        unfinished.countDown()
    }

    override fun succeed() {
        if (!complete) {
            state = SUCCESSFUL
            unfinished.countDown()
        }
    }

    override fun cancel(reason: Throwable?) {
        if (!complete) {
            myReason = reason
            state = CANCELLED
            unfinished.countDown()
        }
    }

    override fun fail(reason: Throwable?) {
        if (!complete) {
            myReason = reason
            state = FAILED
            unfinished.countDown()
        }
    }

    override fun standby(): Request {
        unfinished.await()
        return report()
    }

    override fun standby(delay: Timespan): Request {
        if (!unfinished.await(delay.amount, delay.unit)) throw StandbyTimeoutException(delay)
        return report()
    }

    private fun report(): BasicRequest {
        if (failed) throw RequestFailedException(reason)
        if (cancelled) throw RequestCancelledException(reason)
        if (lost) throw RequestNotAcceptedException()
        Thread.yield()
        return this
    }

    override fun whenState(state: RequestState, action: () -> Unit) {
        if (synchronized(myStates) {
                (state in myStates).also { if (!it) stateRegistry.add(RequestStateObserver(state, action)) }
            }) action.invoke()
    }

    final override fun onSuccess(action: Request.() -> Unit): Request = also {
        postActionSemaphore.acquire()
        try {
            successActions += action
            if (state in FINISHED) runPostActions() else enregister(PostActionObserver())
        } finally {
            postActionSemaphore.release()
        }
    }

    final override fun onFailure(action: Request.() -> Unit): Request = also {
        postActionSemaphore.acquire()
        try {
            failureActions += action
            if (state in FINISHED) runPostActions() else enregister(PostActionObserver())
        } finally {
            postActionSemaphore.release()
        }
    }

    final override fun onCancellation(action: Request.() -> Unit): Request = also {
        postActionSemaphore.acquire()
        try {
            cancellationActions += action
            if (state in FINISHED) runPostActions() else enregister(PostActionObserver())
        } finally {
            postActionSemaphore.release()
        }
    }

    override fun whenFinished(action: Request.() -> Unit): Request = also {
        postActionSemaphore.acquire()
        try {
            successActions += action
            failureActions += action
            cancellationActions += action
            if (state in FINISHED) runPostActions() else enregister(PostActionObserver())
        } finally {
            postActionSemaphore.release()
        }
    }

    override fun propagateTo(other: Request) = whenFinished {
        trace("Propagating state ‹%s› from ‹%s› to ‹%s›.", state, this, other)
        when (state) {
            SUCCESSFUL -> other.succeed()
            FAILED -> other.fail(reason)
            CANCELLED -> other.cancel(reason)
            else -> relax()
        }
    }

    private fun runPostActions() {
        postActionSemaphore.release()
        try {
            if (successful) successActions.forEach { it.invoke(this) }
            if (failed) failureActions.forEach { it.invoke(this) }
            if (cancelled) cancellationActions.forEach { it.invoke(this) }
            successActions.clear()
            failureActions.clear()
            cancellationActions.clear()
        } finally {
            postActionSemaphore.acquire()
        }
    }

    override fun enregister(observer: Observer) = !complete && stateRegistry.add(observer)
    override fun deregister(observer: Observer) = stateRegistry.remove(observer)
    override fun andThen(action: () -> Unit) = also { whenState(SUCCESSFUL, action) }
    override fun swallow() {
        whenFinished {
            when (state) {
                FAILED -> {
                    CoraCom.log.warn(reason, TEXT_REQUEST_FAILED, this@BasicRequest)
                    throw RequestFailedException(reason)
                }
                CANCELLED -> {
                    CoraCom.log.warn(reason, TEXT_REQUEST_CANCELLED, this@BasicRequest)
                    throw RequestCancelledException(reason)
                }
                LOST -> {
                    CoraCom.log.warn(TEXT_REQUEST_LOST, this@BasicRequest)
                }
                else -> relax()
            }
        }
    }

    private inner class PostActionObserver : Observer {
        override fun onNotification(event: Event): Boolean = when (event) {
            is RequestStateChangedEvent -> (event.current in FINISHED).also { complete ->
                if (complete) runPostActions()
            }
            else -> false
        }
    }

    companion object {
        private val TEXT_REQUEST_FAILED = LocalText("RequestFailed1")
        private val TEXT_REQUEST_CANCELLED = LocalText("RequestCancelled1")
        private val TEXT_REQUEST_LOST = LocalText("RequestLost1")
    }
}
