package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.model.RequestList
import com.coradec.coradeck.text.model.LocalText

class BasicRequestList(origin: Origin, private val requests: Iterator<Request>) :
        BasicRequest(origin), RequestList {
    constructor(origin: Origin, requests: Sequence<Request>) : this(origin, requests.iterator())
    constructor(origin: Origin, requests: List<Request>) : this(origin, requests.iterator())

    override fun execute(): Unit = when {
        complete -> relax()
        requests.hasNext() -> requests.next().let { request ->
            if (recipient == null) throw IllegalStateException("Cannot execute when recipien is not set!")
            request.enregister(this)
            if (!request.complete) {
                if (!request.enqueued) recipient!!.inject(request)
            } else process(request, request.state)
        }
        else -> succeed()
    }

    override fun onNotification(event: Event): Boolean = when {
            complete -> false
            event is StateChangedEvent -> {
                val element: Information = event.source
                debug("State Changed: %s %s→%s", element, event.previous, event.current)
                val newState: State = event.current
                process(element, newState)
                newState in FINISHED
            }
            else -> false.also { warn(TEXT_EVENT_NOT_UNDERSTOOD, event) }
        }

    private fun process(element: Information, state: State) = when (state) {
        SUCCESSFUL -> execute()
        FAILED -> fail(if (element is Request) element.reason else null)
        CANCELLED -> cancel()
        else -> relax()
    }

    companion object {
        private val TEXT_EVENT_NOT_UNDERSTOOD = LocalText("EventNotUnderstood1")
    }
}
