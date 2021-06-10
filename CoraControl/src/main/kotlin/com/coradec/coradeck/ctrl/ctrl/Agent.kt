package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient

interface Agent: Recipient {
    /** Triggers the agent to process the next message in its queue. */
    fun trigger()
    /** Waits until all requests so far have been processed. */
    fun synchronize()
}
