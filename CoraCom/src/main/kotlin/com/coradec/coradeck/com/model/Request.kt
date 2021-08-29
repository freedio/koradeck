/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

interface Request: Message {
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
    /** Mark the request as successful. */
    fun succeed()
    /** Mark the request as failed for the specified optional reason. */
    fun fail(reason: Throwable? = null)
    /** Mark the request as cancelled for the specified optional reason. */
    fun cancel(reason: Throwable? = null)
    /** Wait until the request has finished (i. e. has been successful, cancelled, or failed). */
    fun standBy(): Request
    /** Add an action to perform when the request was successful. */
    fun onSuccess(action: Request.() -> Unit): Request
    /** Add an action to perform when the request failed. */
    fun onFailure(action: Request.() -> Unit): Request
    /** Add an action to perform when the request was cancelled. */
    fun onCancellation(action: Request.() -> Unit): Request
    /** Triggers the specified action when the request is finished. */
    fun whenFinished(action: Request.() -> Unit): Request
}
