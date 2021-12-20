/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.RequestState.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.RequestStateChangedEvent
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.model.ItemSet
import com.coradec.coradeck.ctrl.module.CoraControl.IMMEX
import com.coradec.coradeck.ctrl.trouble.LostInformationException
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger

class BasicItemSet(
    origin: Origin,
    items: Sequence<Information>,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime  = validFrom + CoraCom.standardValidity,
    private val processor: Agent? = null
) : BasicRequest(origin, priority, createdAt, session, validFrom, validUpto), ItemSet {
    private val actionItems = items.map { if (it is Notification<*>) it else Notification(it) }
    private var outstanding = AtomicInteger(0)
    private var endState: RequestState = SUCCESSFUL
    private var endProblem: Throwable? = null

    override fun execute() {
        if (actionItems.none()) succeed()
        actionItems.forEach { item ->
            if (!complete) {
                if (if (item.content is Request) (item.content as Request).enregister(this)
                    else item.enregister(this)
                ) outstanding.incrementAndGet()
                if (item.complete) process(item, item.state) else
                    if (!item.enqueued) processor?.accept(item) ?: IMMEX.inject(item)
            }
        }
    }

    override fun onNotification(event: Event): Boolean = when {
        complete -> false
        event is RequestStateChangedEvent -> {
            val element: Request = event.message
            debug("Request state changed: %s %s→%s", element, event.previous, event.current)
            val newState = event.current
            process(element, newState)
            newState in FINISHED
        }
        event is StateChangedEvent -> {
            val element: Notification<*> = event.message
            debug("Notification state changed: %s %s→%s", element, event.previous, event.current)
            val newState = event.current
            process(element, newState)
            newState == NotificationState.PROCESSED
        }
        else -> false.also { warn(TEXT_EVENT_NOT_UNDERSTOOD, event) }
    }

    private fun process(item: Notification<out Information>, state: NotificationState) {
        when (val element = item.content) {
            is Request -> process(element, element.state)
            is Notification<*> -> process(element, element.state)
            else -> process(item, element, state)
        }
        debug("%s/%s: Outstanding: %d", item.content, state, outstanding.get())
    }

    private fun process(notification: Notification<out Information>, element: Information, state: NotificationState) = when(state) {
        NotificationState.PROCESSED -> {
            if (outstanding.decrementAndGet() == 0) when (endState) {
                SUCCESSFUL -> succeed()
                FAILED -> fail(endProblem)
                CANCELLED -> cancel()
                else -> throw IllegalStateException("Illegal end state: $endState")
            } else relax()
        }
        NotificationState.REJECTED, NotificationState.CRASHED -> fail(notification.problem)
        NotificationState.LOST -> fail(LostInformationException(element))
        else -> relax()
    }

    private fun process(element: Request, state: RequestState) {
        if (endState == SUCCESSFUL) when (state) {
            FAILED -> {
                endProblem = element.reason
                endState = FAILED
            }
            CANCELLED -> {
                actionItems.filter { !it.complete }.forEach { if (it is Request) it.cancel() }
                endState = CANCELLED
            }
            else -> relax()
        }
        if (state in FINISHED && outstanding.decrementAndGet() == 0) when (endState) {
            SUCCESSFUL -> succeed()
            FAILED -> fail(endProblem)
            CANCELLED -> cancel()
            else -> relax()
        }
    }

    companion object {
        private val TEXT_EVENT_NOT_UNDERSTOOD = LocalText("EventNotUnderstood1")
    }

}
