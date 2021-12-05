/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.ctrl.model.Task

/** API of an information, market, messaging, event-handling and execution service. */
interface IMMEX {
    /** Load indicator. */
    val load: Int
    /** Statistics snapshot. */
    val stats: String

    /** Schedules the specified task for execution. */
    fun execute(executable: Runnable)
    /** Schedules the specified task for execution with the specified priority. */
    fun execute(executable: Runnable, prio: Priority)
    /** Schedules the specified task for execution. */
    fun execute(task: Task)
    /** Injects the specified information, returning a notification for tracking pogress. */
    fun <I : Information> inject(info: I): Notification<I>
    /** Injects the specified notification. */
    fun <I : Information, N : Notification<I>> inject(notification: N): N
    /** Waits until all current messages have been processed (without preventing new ones to be inserted). */
    fun synchronize()
    /** Registers a number of listeners for broadcast information. */
    fun subscribe(vararg listeners: Recipient)
    /** Unregisters a number of listeners from broadcast reception. */
    fun unsubscribe(vararg listeners: Recipient)
    /** Blocks the specified recipient so that it can't receive dispatched messaged any more. */
    fun block(recipient: Recipient)
    /** Unblocks the specified recipient. */
    fun release(recipient: Recipient)
    /** Prints immediate statistics on the IMMEX state, */
    fun printFullStats()
    /** Wait until there are no more pending messages in CIMMEX. */
    fun standby()
    /** Wait for at most delay until there are no more pending messages in CIMMEX. */
    fun standby(delay: Timespan)
    /** Make sure the IMMEX is not able to shut down until a symmetrical allowShutdown() has been invoked. */
    fun preventShutdown()
    /** Allows the IMMEX to shut down.  Make sure that this method is called only with a symmetrical #preventShutdown() */
    fun allowShutdown()
}
