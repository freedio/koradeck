package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.AgentPool
import java.util.concurrent.LinkedBlockingQueue

class BasicAgentPool<A: Agent>(
    private val lowWaterMark: Int,
    private val highWaterMark: Int,
    private val newAgent: () -> A
) : BasicAgent(), AgentPool<A> {
    val pool = LinkedBlockingQueue<PoolAgent>()
    @Volatile var agents = 0
    @Volatile var backlog = 0

    override fun <I : Information> inject(message: I): I =
        nextAgent().inject(message).also { ++backlog }

    private fun nextAgent(): Agent {
        val agent = pool.poll()
        if (agent != null) return agent
        if (agents >= highWaterMark) return pool.take()
        ++agents
        return PoolAgent(newAgent.invoke())
    }

    private fun insert(agent: PoolAgent) {
        --backlog
        if (backlog > agents || agents < lowWaterMark || pool.isEmpty()) pool.add(agent) else --agents
    }

    inner class PoolAgent(val wrapped: Agent): BasicAgent() {
        override fun <I : Information> inject(message: I): I =
            try {
                wrapped.inject(message.also {
                    if (it is Request) it.whenFinished { insert(this@PoolAgent) }
                })
            } catch (e: Exception) {
                if (message is Request) {
                    this@BasicAgentPool.error(e)
                    message.apply { fail(e) }
                } else throw e
            }
    }
}
