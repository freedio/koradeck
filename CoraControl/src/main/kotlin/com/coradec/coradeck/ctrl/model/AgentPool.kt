package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl

interface AgentPool: Agent {
    companion object {
        operator fun <AgentType : Agent> invoke(low: Int, high: Int, generator: () -> AgentType) =
            CoraControl.createAgentPool(low, high, generator)
    }
}
