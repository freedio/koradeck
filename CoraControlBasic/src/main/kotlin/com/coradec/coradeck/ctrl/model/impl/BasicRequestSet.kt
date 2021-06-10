package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.com.model.State.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentSkipListSet

class BasicRequestSet(origin: Origin, recipient: Recipient, private val requests: List<Request>) :
        BasicRequest(origin, recipient), RequestSet {
    private val tracking = ConcurrentLinkedQueue<Request>()

    override fun execute() {
        requests.mapNotNull { request ->
            when (this@BasicRequestSet.state) {
                in FINISHED -> null
                else -> request
            }
        }.filter { request ->
            request.state !in FINISHED
        }.mapTo(tracking) { request -> recipient.inject(request.enregister(this)) }.ifEmpty { succeed() }
    }

    override fun notify(event: Event) = when {
        complete -> relax()
        event is StateChangedEvent -> {
            val element: Information = event.source
            debug("State Changed: %s %s→%s", element, event.previous, event.current)
            if (!tracking.remove(element)) throw IllegalStateException("%s is not contained in %s".format(element, tracking))
            when (event.current) {
                SUCCESSFUL -> if (tracking.isEmpty()) this@BasicRequestSet.succeed() else relax()
                FAILED -> this@BasicRequestSet.fail(if (element is Request) element.problem else null)
                CANCELLED -> this@BasicRequestSet.cancel().also { tracking.map { it.cancel() }; tracking.clear() }
                else -> relax()
            }
        }
        else -> warn(TEXT_EVENT_NOT_UNDERSTOOD, event)
    }.let { true }

    companion object {
        private val TEXT_EVENT_NOT_UNDERSTOOD = LocalText("EventNotUnderstood")
    }

}
