/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.impl.BasicNotification
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.trouble.StandbyTimeoutException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

interface Notification<I: Information> : Information {
    /** The content of the message. */
    val content: I
    /** The state of the notification. */
    val state: NotificationState
    /** A list of state(transition)s (in order) the notification went through. */
    val states: EnumSet<NotificationState>
    /** Whether the notification is new and was never enqueued nor dispatched. */
    val new: Boolean
    /** Whether the notification was ever enqueued. */
    val enqueued: Boolean
    /** Whether the notification was ever dispatched. */
    val dispatched: Boolean
    /** Whether the notification was ever delivered. */
    val delivered: Boolean
    /** Whether the notification was rejected. */
    val rejected: Boolean
    /** Whether the notification was ever processed. */
    val processed: Boolean
    /** Whether the notification ever crashed. */
    val crashed: Boolean
    /** Whether the notification was lost. */
    val lost: Boolean
    /** Number of observers attached to the notification. */
    val observerCount: Int
    /** Indicates whether the notification is complete (processed). */
    val complete: Boolean
    /** The problem description (only meaningful with states REJECTED and CRASHED). */
    val reason: Throwable?

    /** Registers the specified observer for state changes. @return `true` if the observer was enregistered. */
    fun enregister(observer: Observer): Boolean
    /** Removes the specified observer from the state change registry. @return `true` if the observer was deregistered. */
    fun deregister(observer: Observer): Boolean
    /** Marks the notification as enqueued. */
    fun enqueue()
    /** Marks the notification as dispatched. */
    fun dispatch()
    /** Marks the notification as delivered. */
    fun deliver()
    /** Marks the notification as processed. */
    fun process()
    /** Marks the notification as lost. */
    fun discard()
    /** Marks the notification as crashed. */
    fun crash(reason: Throwable)
    /** Marks the notification as rejected. */
    fun reject(reason: Throwable)
    /** Triggers the specified action whenever the specified state is reached. */
    fun whenState(state: NotificationState, action: () -> Unit)
    /** Triggers the specified action when a terminal state has been reached. */
    fun whenFinished(action: Notification<I>.() -> Unit)
    /**
     * Wait for the notification to finish (i. e. its request to become successful, cancelled, or failed, or its information to
     * be processed).  Fluid.
     */
    fun standby(): Notification<*>
    /**
     * Wait for at most delay until the notification be finished (i. e. its request to become successful, cancelled, or failed,
     * or its information to be processed).  Fluid.
     */
    @Throws(StandbyTimeoutException::class) fun standby(delay: Timespan): Notification<*>
    /** Executes the specified action if the notification was processed (see standby). */
    infix fun andThen(action: () -> Unit)
    /** Discard the notification, but still keep track when it fails or is cancelled. */
    fun swallow()

    companion object {
        val LOST_ITEMS = LinkedBlockingQueue<Notification<*>>()
        operator fun <I: Information> invoke(content: I): Notification<I> = BasicNotification(content)
    }
}
