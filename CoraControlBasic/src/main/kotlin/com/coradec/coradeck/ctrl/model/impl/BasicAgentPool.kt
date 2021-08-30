package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.State.PROCESSED
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.model.AgentPool
import java.util.concurrent.LinkedBlockingQueue

class BasicAgentPool<AgentType : Agent>(
    private val low: Int,
    private val high: Int,
    private val generator: () -> AgentType
) : BasicAgent(), AgentPool {
    private val agents = LinkedBlockingQueue<AgentType>()

    @Transient
    var agentCount = 0
    @Transient
    var processing = 0

    override fun <I : Information> inject(message: I): I = with(agents.poll() ?: makeOrWaitForAgent()) {
        ++processing
        inject(message.apply { whenState(PROCESSED) {
            if (processing > agentCount || agents.size < low || agents.size == 0) agents.put(this@with)
        } })
    }

    private fun makeOrWaitForAgent(): AgentType = if (agentCount < high) generate() else agents.take()
    private fun generate(): AgentType = generator.invoke().also { ++agentCount }
}
