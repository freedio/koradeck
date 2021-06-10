package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.model.RequestList
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.ConcurrentLinkedQueue

class BasicRequestList(origin: Origin, recipient: Recipient, requests: List<Request>) :
        BasicRequest(origin, recipient), RequestList {
    private val tracking = ConcurrentLinkedQueue(requests)

    override fun execute() {
        when {
            complete -> relax()
            tracking.isEmpty() -> succeed()
            else -> recipient.inject(tracking.remove().enregister(this))
        }
    }

    override fun notify(event: Event): Boolean = when {
        complete -> relax()
        event is StateChangedEvent -> {
            val element: Information = event.source
            debug("State Changed: %s %s→%s", element, event.previous, event.current)
            when (event.current) {
                State.SUCCESSFUL -> execute()
                State.FAILED -> this@BasicRequestList.fail(if (element is Request) element.problem else null)
                State.CANCELLED -> this@BasicRequestList.cancel().also { tracking.map { it.cancel() }; tracking.clear() }
                else -> relax()
            }
        }
        else -> warn(TEXT_EVENT_NOT_UNDERSTOOD, event)
    }.let { true }

    companion object {
        private val TEXT_EVENT_NOT_UNDERSTOOD = LocalText("EventNotUnderstood")
    }
}
