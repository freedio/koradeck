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
    val problem: Throwable?
    /** Mark the request as successful. */
    fun succeed()
    /** Mark the request as failed with the specified optional problem. */
    fun fail(problem: Throwable? = null)
    /** Mark the request as cancelled. */
    fun cancel()
    /** Wait until the request has finished (i. e. has been successful, cancelled, or failed). */
    fun standBy()
}
