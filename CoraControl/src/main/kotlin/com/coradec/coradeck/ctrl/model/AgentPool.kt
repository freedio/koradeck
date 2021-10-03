/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl

interface AgentPool: Agent {
    /** Returns a string snapshot containing the statistics of the pool at the time of invocation. */
    val stats: String

    companion object {
        operator fun <AgentType : Agent> invoke(low: Int, high: Int, generator: () -> AgentType) =
            CoraControl.createAgentPool(low, high, generator)
    }

    /** Shuts the pool down after synchronizing it (making sure that no requests are pending). */
    fun shutdown()
}
