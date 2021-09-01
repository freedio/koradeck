package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Information.Companion.LOST_ITEMS
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.com.model.StateObserver
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Expiration.Companion.never_expires
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.*
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime
import java.util.*
import java.util.Collections.binarySearch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

@Suppress("UNCHECKED_CAST")
open class BasicInformation(
    override val origin: Origin,
    override val urgent: Boolean = false,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val session: Session = Session.current,
    override val expires: Expiration = never_expires
) : Logger(), Information {
    private val stateRegistry = CopyOnWriteArraySet<Observer>()
    private val myStates = CopyOnWriteArrayList<State>().apply { add(NEW) }
    override val states: List<State> = Collections.unmodifiableList(myStates)
    override val new: Boolean get() = states.singleOrNull() == NEW
    override val enqueued: Boolean get() = ENQUEUED in states
    override val dispatched: Boolean get() = DISPATCHED in states
    override val observerCount: Int get() = stateRegistry.size
    override val copy: Information get() = this
    override var state: State
        get() = myStates.last()
        set(state) {
            fun invert(i: Int): Int = if (i < 0) -i-1 else i
            fun addState(newstate: State) { myStates.add(invert(binarySearch(myStates, newstate)), newstate) }
            interceptSetState(state)
            if (state !in myStates) {
                val event = StateChangedEvent(here, this, myStates.last(), state.apply { addState(this) })
                stateRegistry.forEach { if (it.notify(event)) stateRegistry.remove(it) }
            }
        }

    protected open fun interceptSetState(state: State) = relax()

    override fun withDefaultRecipient(target: Recipient?) = BasicMessage(origin, urgent, createdAt, session, expires, target)
    override fun withRecipient(target: Recipient) = withDefaultRecipient(target)

    override fun enregister(observer: Observer) = stateRegistry.add(observer)
    override fun deregister(observer: Observer) = stateRegistry.remove(observer)
    override fun enqueue() = also { state = ENQUEUED }
    override fun dispatch() = also { state = DISPATCHED }
    override fun delivered() = also { state = DELIVERED }
    override fun processed() = also { state = PROCESSED }

    override fun miss() = also {
        state = LOST
        LOST_ITEMS += this
    }

    override fun toString(): String =
        "%s(%s)".format(shortClassname, this.properties.filterNot { it.key == "copy" }.formatted)
    override fun whenState(state: State, action: () -> Unit) {
        stateRegistry.add(StateObserver(state, action))
    }
}
