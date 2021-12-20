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
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.model.RequestList
import com.coradec.coradeck.ctrl.module.CoraControl.IMMEX
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.ZonedDateTime

class BasicRequestList(
    origin: Origin,
    private val requests: Iterator<Request>,
    priority: Priority = Priority.defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime  = validFrom + CoraCom.standardValidity,
    private val processor: Agent? = null
) : BasicRequest(origin, priority, createdAt, session, validFrom, validUpto), RequestList {
    constructor(
        origin: Origin,
        requests: Sequence<Request>,
        priority: Priority = Priority.defaultPriority,
        createdAt: ZonedDateTime = ZonedDateTime.now(),
        session: Session = Session.current,
        validFrom: ZonedDateTime = createdAt,
        validUpto: ZonedDateTime  = validFrom + CoraCom.standardValidity,
        processor: Agent? = null
    ) : this(origin, requests.iterator(), priority, createdAt, session, validFrom, validUpto, processor)

    override fun execute(): Unit = when {
        complete -> relax()
        requests.hasNext() -> requests.next().let { request ->
            request.enregister(this)
            if (!request.complete) {
                if (!request.enqueued) processor?.accept(request) ?: IMMEX.inject(request)
            } else process(request, request.state)
        }
        else -> succeed()
    }

    override fun onNotification(event: Event): Boolean = when {
        complete -> false
        event is RequestStateChangedEvent -> {
            val element: Request = event.message
            trace("Request state changed: %s %s→%s", element, event.previous, event.current)
            val newState: RequestState = event.current
            process(element, newState)
            newState in FINISHED
        }
        else -> false.also { warn(TEXT_EVENT_NOT_UNDERSTOOD, event) }
    }

    private fun process(element: Request, state: RequestState) = when (state) {
        SUCCESSFUL -> execute()
        FAILED -> fail(element.reason)
        CANCELLED -> cancel()
        else -> relax()
    }

    companion object {
        private val TEXT_EVENT_NOT_UNDERSTOOD = LocalText("EventNotUnderstood1")
    }
}
