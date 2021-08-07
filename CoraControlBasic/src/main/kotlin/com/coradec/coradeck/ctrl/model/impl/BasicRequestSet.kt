package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.text.model.LocalText

class BasicRequestSet(origin: Origin, recipient: Recipient, private val requests: Sequence<Request>) :
    BasicRequest(origin, recipient), RequestSet {
    constructor(origin: Origin, recipient: Recipient, requests: List<Request>) : this(origin, recipient, requests.asSequence())

    var outstanding = 0
    var endState: State = SUCCESSFUL
    var endProblem: Throwable? = null

    override fun execute() {
        if (requests.none()) succeed()
        requests.forEach { request ->
            if (!complete) {
                if (request.enregister(this)) ++outstanding
                if (request.complete) process(request, request.state) else if (!request.enqueued) recipient.inject(request)
            }
        }
    }

    override fun notify(event: Event) = when {
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
