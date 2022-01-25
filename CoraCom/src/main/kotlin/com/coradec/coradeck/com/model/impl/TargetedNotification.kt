/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime
import java.util.*

open class TargetedNotification<I: Information>(
    val notification: Notification<I>,
    override val recipient: Recipient,
    override val origin: Origin = notification.origin,
    priority: Priority = notification.priority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = notification.validFrom,
    validUpTo: ZonedDateTime = notification.validUpTo,
) : BasicInformation(origin, priority, createdAt, session, validFrom, validUpTo), Message<I> {
    override val content: I get() = notification.content
    override val states: EnumSet<NotificationState> = notification.states
    override var state: NotificationState = notification.state
    override val new: Boolean get() = notification.new
    override val enqueued: Boolean get() = notification.enqueued
    override val dispatched: Boolean get() = notification.dispatched
    override val delivered: Boolean get() = notification.delivered
    override val rejected: Boolean get() = notification.rejected
    override val processed: Boolean get() = notification.processed
    override val crashed: Boolean get() = notification.crashed
    override val lost: Boolean get() = notification.lost
    override val reason: Throwable? get() = notification.reason
    override val complete: Boolean get() = notification.complete
    override val observerCount: Int get() = notification.observerCount

    override fun enregister(observer: Observer): Boolean = notification.enregister(observer)
    override fun deregister(observer: Observer): Boolean = notification.deregister(observer)
    override fun enqueue() = notification.enqueue()
    override fun dispatch() = notification.dispatch()
    override fun deliver() = notification.deliver()
    override fun reject(reason: Throwable) = notification.reject(reason)
    override fun process() = notification.process()
    override fun discard() = notification.discard()
    override fun crash(reason: Throwable) = notification.crash(reason)
    override fun whenState(state: NotificationState, action: () -> Unit) = notification.whenState(state, action)
    override fun standby(): Notification<*> = notification.standby()
    override fun standby(delay: Timespan): Notification<*> = notification.standby(delay)
    override fun andThen(action: () -> Unit) = notification.andThen(action)
    override fun swallow() = notification.swallow()
    override fun whenFinished(action: Notification<I>.() -> Unit) = notification.whenFinished(action)
}
