package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Command
import com.coradec.coradeck.com.model.Recipient
import kotlin.reflect.KClass

interface Agent: Recipient {
    val queueSize: Int
    /** Triggers the agent to process the next message in its queue. */
    fun trigger()
    /** Waits until all requests so far have been processed. */
    fun synchronize()
    /** Approves the specified command(s). */
    fun approve(vararg cmd: KClass<out Command>)
    /** Disapproves the specified command(s). */
    fun disapprove(vararg cmd: KClass<out Command>)
}
