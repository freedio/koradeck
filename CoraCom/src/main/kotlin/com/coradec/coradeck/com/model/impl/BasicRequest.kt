package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.trouble.RequestCancelledException
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.core.model.Origin
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Semaphore

open class BasicRequest(origin: Origin, recipient: Recipient) : BasicMessage(origin, recipient), Request {
    private var myProblem: Throwable? = null
    private val unfinished = CountDownLatch(1)
    override val problem: Throwable? get() = myProblem
    override val successful: Boolean get() = state == SUCCESSFUL
    override val failed: Boolean get() = state == FAILED
    override val cancelled: Boolean get() = state == CANCELLED
    override val complete: Boolean get() = state in COMPLETION_STATES
    private val successActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val failureActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val cancellationActions: MutableList<Request.() -> Unit> = mutableListOf()
    private val postActionSemaphore = Semaphore(1)

    override fun succeed() {
        state = SUCCESSFUL
        unfinished.countDown()
    }

    override fun cancel() {
        state = CANCELLED
        unfinished.countDown()
    }

    override fun fail(problem: Throwable?) {
        myProblem = problem
        state = FAILED
        unfinished.countDown()
    }

    override fun standBy() {
        unfinished.await()
        if (problem != null) throw problem!!
        if (failed) throw RequestFailedException()
        if (cancelled) throw RequestCancelledException()
    }

    override fun onSuccess(action: Request.() -> Unit) {
        postActionSemaphore.acquire()
        try {
            successActions += action
            if (state in FINISHED) runPostActions()
        } finally {
            postActionSemaphore.release()
        }
    }

    override fun onFailure(action: Request.() -> Unit) {
        postActionSemaphore.acquire()
        try {
            failureActions += action
            if (state in FINISHED) runPostActions()
        } finally {
            postActionSemaphore.release()
        }
    }

    override fun onCancellation(action: Request.() -> Unit) {
        postActionSemaphore.acquire()
        try {
            cancellationActions += action
            if (state in FINISHED) runPostActions()
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

    companion object {
        private val COMPLETION_STATES = setOf(SUCCESSFUL, FAILED, CANCELLED)
    }
}
