package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

class BasicRequestSet(origin: Origin, recipient: Recipient, private val requests: Sequence<Request>) :
        BasicRequest(origin, recipient), RequestSet {
    constructor(origin: Origin, recipient: Recipient, requests: List<Request>) : this(origin, recipient, requests.asSequence())

    var outstanding = 0

    override fun execute() {
        if (requests.none()) succeed()
        requests.forEach { request ->
            if (!complete) {
                if (request.enregister(this)) ++outstanding
                if (!request.complete) {
                    if (!request.enqueued) recipient.inject(request)
                } else process(request, request.state)
            }
        }
    }

    override fun notify(event: Event) = when {
        complete -> relax()
        event is StateChangedEvent -> {
            val element: Information = event.source
            trace("State Changed: %s %s→%s", element, event.previous, event.current)
            val newState = event.current
            process(element, newState)
        }
        else -> warn(TEXT_EVENT_NOT_UNDERSTOOD, event)
    }.let { true }

    private fun process(element: Information, state: State) = when (state) {
        SUCCESSFUL -> if (--outstanding == 0) succeed() else relax()
        FAILED -> fail(if (element is Request) element.problem else null)
        CANCELLED -> cancel().also { requests.filter { !it.complete }.forEach { it.cancel() } }
        else -> relax()
    }

    companion object {
        private val TEXT_EVENT_NOT_UNDERSTOOD = LocalText("EventNotUnderstood")
    }

}
