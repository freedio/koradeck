/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient
import kotlin.reflect.KClass

/** API of an information, market, messaging, event-handling and execution service. */
interface IMMEX {
    /** Schedules the specified task for execution. */
    fun execute(task: Runnable)
    /** Injects the specified message to the dispatcher for targeting or broadcasting. */
    fun <I: Information> inject(message: I): I
    /** Waits until all current messages have been processed (without preventing new ones to be inserted). */
    fun synchronize()
    /** Registers a number of listeners for broadcast information of the specified type. */
    fun plugin(klass: KClass<out Information>, vararg listener: Recipient)
    /** Unregisters a number of listeners from broadcast reception. */
    fun unplug(vararg listener: Recipient)
}
