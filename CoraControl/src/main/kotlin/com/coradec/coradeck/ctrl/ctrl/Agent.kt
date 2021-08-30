package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin

interface Agent: Origin, Recipient {
    /** Waits until all requests so far have been processed. */
    fun synchronize()

    /** Injects the specified information to the agent's message queue. */
    fun <I : Information> inject(message: I): I
}
