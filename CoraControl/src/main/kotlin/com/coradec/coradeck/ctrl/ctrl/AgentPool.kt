package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.ctrl.module.CoraControl

interface AgentPool<A: Agent>: Agent {
    companion object {
        operator fun <A: Agent> invoke(lowWaterMark: Int, highWaterMark: Int, newAgent: () -> A): AgentPool<A> =
            CoraControl.createAgentPool(lowWaterMark, highWaterMark, newAgent)
    }
}