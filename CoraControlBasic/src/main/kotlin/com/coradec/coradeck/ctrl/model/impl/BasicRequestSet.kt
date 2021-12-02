/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.RequestState
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.RequestState.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.RequestStateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.ctrl.module.CoraControl.IMMEX
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class BasicRequestSet(
    origin: Origin,
    private val requests: Sequence<Request>,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC),
    private val processor: Agent? = null
) : BasicRequest(origin, priority, createdAt, session, validFrom, validUpto), RequestSet {
    private var outstanding = 0
    private var endState: RequestState = SUCCESSFUL
    private var endProblem: Throwable? = null

    override fun execute() {
        if (requests.none()) succeed()
        requests.forEach { request ->
            if (!complete) {
                if (request.enregister(this)) ++outstanding
                if (request.complete) process(request, request.state) else
                    if (!request.enqueued) processor?.accept(request) ?: IMMEX.inject(request)
            }
        }
    }

    override fun onNotification(event: Event): Boolean = when {
            complete -> false
            event is RequestStateChangedEvent -> {
                val element: Request = event.message
                trace("Request state changed: %s %s→%s", element, event.previous, event.current)
                val newState = event.current
                process(element, newState)
                newState in FINISHED
            }
            else -> false.also { warn(TEXT_EVENT_NOT_UNDERSTOOD, event) }
        }

    private fun process(element: Request, state: RequestState) {
        if (endState == SUCCESSFUL) when (state) {
            FAILED -> {
                endProblem = element.reason
                endState = FAILED
            }
            CANCELLED -> {
                requests.filter { !it.complete }.forEach { it.cancel() }
                endState = CANCELLED
            }
            else -> relax()
        }
        if (state in FINISHED && --outstanding == 0) when (endState) {
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
