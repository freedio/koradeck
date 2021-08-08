package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.trouble.RequestCancelledException
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore

open class BasicRequest(
    origin: Origin,
    recipient: Recipient,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires,
    urgent: Boolean = false
) : BasicMessage(origin, recipient, created, session, expires, urgent), Request {
    private var myReason: Throwable? = null
    private val unfinished = CountDownLatch(1)
    override val reason: Throwable? get() = myReason
    override val successful: Boolean get() = state == SUCCESSFUL
    override val failed: Boolean get() = state == FAILED
    override val cancelled: Boolean get() = state == CANCELLED
    override val complete: Boolean get() = state in FINISHED
    private val successActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val failureActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val cancellationActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val postActionSemaphore = Semaphore(1)

    override fun succeed() {
        state = SUCCESSFUL
        unfinished.countDown()
    }

    override fun cancel(reason: Throwable?) {
        myReason = reason
        state = CANCELLED
        unfinished.countDown()
    }

    override fun fail(reason: Throwable?) {
        myReason = reason
        state = FAILED
        unfinished.countDown()
    }

    override fun standBy() {
        unfinished.await()
        if (reason != null) throw reason!!
        if (failed) throw RequestFailedException()
        if (cancelled) throw RequestCancelledException()
    }

    override fun onSuccess(action: Request.() -> Unit): Request = also {
        postActionSemaphore.acquire()
        try {
            successActions += action
            if (state in FINISHED) runPostActions() else enregister(PostActionObserver())
        } finally {
            postActionSemaphore.release()
        }
    }

    override fun onFailure(action: Request.() -> Unit): Request = also {
        postActionSemaphore.acquire()
        try {
            failureActions += action
            if (state in FINISHED) runPostActions() else enregister(PostActionObserver())
        } finally {
            postActionSemaphore.release()
        }
    }

    override fun onCancellation(action: Request.() -> Unit): Request = also {
        postActionSemaphore.acquire()
        try {
            cancellationActions += action
            if (state in FINISHED) runPostActions() else enregister(PostActionObserver())
        } finally {
            postActionSemaphore.release()
        }
    }

    private fun runPostActions() {
        if (successful) successActions.forEach { it.invoke(this) }
        if (failed) failureActions.forEach { it.invoke(this) }
        if (cancelled) cancellationActions.forEach { it.invoke(this) }
        successActions.clear()
        failureActions.clear()
        cancellationActions.clear()
    }

    override fun enregister(observer: Observer) = !complete && super.enregister(observer)

    private inner class PostActionObserver : Observer {
        override fun notify(event: Event): Boolean = when (event) {
            is StateChangedEvent -> (event.current in FINISHED).also { complete ->
                if (complete) runPostActions()
            }
            else -> false
        }

    }
}
