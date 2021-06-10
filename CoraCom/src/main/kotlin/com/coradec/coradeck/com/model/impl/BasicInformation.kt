package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Expiration.Companion.never_expires
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime
import java.util.*

@Suppress("UNCHECKED_CAST")
open class BasicInformation(
        override val origin: Origin,
        override val createdAt: ZonedDateTime = ZonedDateTime.now(),
        override val session: Session = Session.current,
        override val expires: Expiration = never_expires
) : Logger(), Information {
    private val stateRegistry = mutableSetOf<Observer>()

    override val urgent: Boolean = false
    private val myStates = mutableListOf(NEW)
    override val states: List<State> = Collections.unmodifiableList(myStates)
    override val enqueued: Boolean get() = ENQUEUED in states
    override val observerCount: Int get() = stateRegistry.size
    override var state: State
        get() = myStates.last()
        set(state) {
            if (state !in myStates) {
                val event = StateChangedEvent(here, this, myStates.last(), state.apply { myStates += this })
                stateRegistry.forEach { if (it.notify(event)) stateRegistry.remove(it) }
            }
        }

    override fun <I : Information> enregister(observer: Observer): I = also { stateRegistry+= observer } as I
    override fun <I : Information> deregister(observer: Observer): I = also { stateRegistry -= observer } as I
}
