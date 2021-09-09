package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.ctrl.module.CoraControl.IMMEX
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class BasicRequestSet(
    origin: Origin,
    val requests: Sequence<Request>,
    urgent: Boolean = false,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    target: Recipient? = null,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
) : BasicRequest(origin, urgent, createdAt, session, target = target, validFrom, validUpto), RequestSet {
    constructor(origin: Origin, requests: List<Request>) : this(origin, requests.asSequence())
    override val copy get() = BasicRequestSet(origin, requests, urgent, createdAt, session, recipient, validFrom, validUpTo)
    override fun copy(recipient: Recipient?) = BasicRequestSet(origin, requests, urgent, createdAt, session, recipient, validFrom, validUpTo)

    var outstanding = 0
    var endState: State = SUCCESSFUL
    var endProblem: Throwable? = null

    override fun execute() {
        if (requests.none()) succeed()
        requests.forEach { request ->
            if (!complete) {
                if (request.enregister(this)) ++outstanding
                if (request.complete) process(request, request.state) else if (!request.enqueued) IMMEX.inject(request)
            }
        }
    }

    override fun onNotification(event: Event): Boolean = when {
            complete -> false
            event is StateChangedEvent -> {
                val element: Information = event.source
                trace("State Changed: %s %s→%s", element, event.previous, event.current)
                val newState = event.current
                process(element, newState)
                newState in FINISHED
            }
            else -> false.also { warn(TEXT_EVENT_NOT_UNDERSTOOD, event) }
        }

    private fun process(element: Information, state: State) {
        if (endState == SUCCESSFUL) when (state) {
            FAILED -> {
                if (element is Request) endProblem = element.reason
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
