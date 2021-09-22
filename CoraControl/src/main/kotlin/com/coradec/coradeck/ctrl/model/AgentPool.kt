/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl

interface AgentPool: Agent {
    val stats: String

    companion object {
        operator fun <AgentType : Agent> invoke(low: Int, high: Int, generator: () -> AgentType) =
            CoraControl.createAgentPool(low, high, generator)
    }
}
