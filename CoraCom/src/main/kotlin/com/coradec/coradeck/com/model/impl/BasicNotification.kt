/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.Notification.Companion.LOST_ITEMS
import com.coradec.coradeck.com.model.NotificationState.*
import com.coradec.coradeck.com.model.NotificationState.Companion.TERMINAL
import com.coradec.coradeck.com.model.RequestState.SUCCESSFUL
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.properties
import com.coradec.coradeck.core.util.shortClassname
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.CountDownLatch

open class BasicNotification<I : Information>(
    override val content: I,
    origin: Origin = content.origin,
    priority: Priority = content.priority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = content.validFrom,
    validUpTo: ZonedDateTime = content.validUpTo
) : BasicInformation(origin, priority, createdAt, session, validFrom, validUpTo), Notification<I> {
    private val unfinished = CountDownLatch(1)
    private val stateRegistry = CopyOnWriteArraySet<Observer>()
    private val myStates = CopyOnWriteArrayList<NotificationState>().apply { add(NEW) }
    override val states: EnumSet<NotificationState> get() = EnumSet.copyOf(myStates)
    override val new: Boolean get() = states.singleOrNull() == NEW
    override val enqueued: Boolean get() = ENQUEUED in myStates
    override val dispatched: Boolean get() = DISPATCHED in myStates
    override val delivered: Boolean get() = DELIVERED in myStates
    override val rejected: Boolean get() = REJECTED in myStates
    override val processed: Boolean get() = PROCESSED in myStates
    override val crashed: Boolean get() = CRASHED in myStates
    override val lost: Boolean get() = LOST in myStates
    override val complete: Boolean get() = PROCESSED in myStates
    override var reason: Throwable? = null
    override val observerCount: Int get() = stateRegistry.size
    override var state: NotificationState
        get() = synchronized(myStates) { states.last() }
        set(state) {
            synchronized(myStates) {
                if (state !in myStates) {
                    val event = StateChangedEvent(here, this, myStates.last(), state.apply { myStates += this })
                    stateRegistry.forEach { if (it.notify(event)) stateRegistry.remove(it) }
                }
            }
        }

    override fun enregister(observer: Observer) =
        stateRegistry.add(observer)

    override fun deregister(observer: Observer) =
        stateRegistry.remove(observer)

    override fun enqueue() {
        state = ENQUEUED
        if (content is Request) (content as Request).enqueue()
    }

    override fun dispatch() {
        myStates.remove(LOST)
        LOST_ITEMS.remove(this)
        state = DISPATCHED
        if (content is Request) (content as Request).dispatch()
    }

    override fun deliver() {
        state = DELIVERED
        if (content is Request) (content as Request).deliver()
    }

    override fun reject(reason: Throwable) {
        this.reason = reason
        state = REJECTED
        if (content is Request) (content as Request).fail(reason)
    }

    override fun process() {
        state = PROCESSED
        if (content is Request) (content as Request).process()
        unfinished.countDown()
    }

    override fun crash(reason: Throwable) {
        this.reason = reason
        state = CRASHED
        if (content is Request) (content as Request).fail(reason)
        unfinished.countDown()
    }

    override fun discard() {
        LOST_ITEMS += this
        state = LOST
        if (content is Request) (content as Request).discard()
        unfinished.countDown()
    }

    override fun whenState(state: NotificationState, action: () -> Unit) {
        if (synchronized(myStates) {
                (state in myStates).also { if (!it) stateRegistry.add(StateObserver(action, state)) }
            }) action.invoke()
    }

    override fun standby(): Notification<*> = also {
        when (content) {
            is Request -> (content as Request).standby()
            is Notification<*> -> (content as Notification<*>).standby()
            else -> unfinished.await()
        }
    }

    override fun standby(delay: Timespan): Notification<*> = also {
        when (content) {
            is Request -> (content as Request).standby(delay)
            is Notification<*> -> (content as Notification<*>).standby(delay)
            else -> unfinished.await(delay.amount, delay.unit)
        }
    }

    override fun toString(): String =
        "%s(%s)".format(shortClassname, properties.formatted)

    override fun andThen(action: () -> Unit) {
        if (content is Request) (content as Request).whenState(SUCCESSFUL, action) else whenState(PROCESSED, action)
    }

    override fun swallow() {
        whenState(LOST) { CoraCom.log.warn(TEXT_NOTIFICATION_LOST, this@BasicNotification) }
        whenState(REJECTED) { CoraCom.log.warn(TEXT_NOTIFICATION_REJECTED, this@BasicNotification) }
        whenState(CRASHED) { CoraCom.log.warn(TEXT_NOTIFICATION_CRASHED, this@BasicNotification) }
    }

    companion object {
        private val TEXT_NOTIFICATION_LOST = LocalText("MotificationLost1")
        private val TEXT_NOTIFICATION_REJECTED = LocalText("NotificationRejected1")
        private val TEXT_NOTIFICATION_CRASHED = LocalText("NotificationCrashed1")
    }

    override fun whenFinished(action: Notification<I>.() -> Unit) {
        if (synchronized(myStates) {
                (state in TERMINAL).also { if (!it) stateRegistry.add(StateObserver({ action.invoke(this) }, TERMINAL)) }
            }) action.invoke(this)
    }
}
