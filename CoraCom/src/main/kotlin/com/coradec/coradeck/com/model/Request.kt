/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.trouble.StandbyTimeoutException
import java.util.*

interface Request: Event {
    /** The state of the message. */
    val state: RequestState
    /** A list of state(transition)s (in order) the message went through. */
    val states: EnumSet<RequestState>
    /** Whether the message is new and was never enqueued nor dispatched. */
    val new: Boolean
    /** Whether the message was ever enqueued. */
    val enqueued: Boolean
    /** Whether the message was ever dispatched. */
    val dispatched: Boolean
    /** Whether the message was ever delivered. */
    val delivered: Boolean
    /** Whether the message was ever processed. */
    val processed: Boolean
    /** Indicates if the request was lost. */
    val lost: Boolean
    /** Indicates if the request was successful. */
    val successful: Boolean
    /** Indicates if the request failed. */
    val failed: Boolean
    /** Indicates if the request was cancelled. */
    val cancelled: Boolean
    /** Indicates whether the request is complete (successful, failed, or cancelled). */
    val complete: Boolean
    /** The problem that made the request fail, if it failed.  A request can fail without giving a reasin though. */
    val reason: Throwable?
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
    /** Mark the request as successful. */
    fun succeed()
    /** Mark the request as failed for the specified optional reason. */
    fun fail(reason: Throwable? = null)
    /** Mark the request as cancelled for the specified optional reason. */
    fun cancel(reason: Throwable? = null)
    /** Wait for the request to finish (i. e. become successful, cancelled, or failed).  Fluid. */
    fun standby(): Request
    /** Wait for at most delay until the request be finished (i. e. become successful, cancelled, or failed).  Fluid. */
    @Throws(StandbyTimeoutException::class) fun standby(delay: Timespan): Request
    /** Triggers the specified action whenever the specified state is reached. */
    fun whenState(state: RequestState, action: () -> Unit)
    /** Add an action to perform when the request was successful.  Fluid. */
    fun onSuccess(action: Request.() -> Unit): Request
    /** Add an action to perform when the request failed.  Fluid. */
    fun onFailure(action: Request.() -> Unit): Request
    /** Add an action to perform when the request was cancelled. Fluid. */
    fun onCancellation(action: Request.() -> Unit): Request
    /** Triggers the specified action when the request is finished.  Fluid. */
    fun whenFinished(action: Request.() -> Unit): Request
    /** Propages the completion state to the specified request.  Fluid. */
    infix fun propagateTo(other: Request): Request
    /** Registers the specified observer for state changes. @return `true` if the observer was enregistered. */
    fun enregister(observer: Observer): Boolean
    /** Removes the specified observer from the state change registry. @return `true` if the observer was deregistered. */
    fun deregister(observer: Observer): Boolean
    /** Executes the specified action if the request was successful (see standby). */
    infix fun andThen(action: () -> Unit): Request

    /** The number of observers on the request. */
    val observerCount: Int
}
