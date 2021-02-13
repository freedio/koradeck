package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient

interface Agent: Recipient {
    /** Injects the specified information into the agent's queue. */
    fun <I: Information> inject(info: I): I
    /** Triggers the agent to process the next message in its queue. */
    fun trigger()
}
