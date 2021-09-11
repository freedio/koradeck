package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.State.PROCESSED
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.model.AgentPool
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class BasicAgentPool<AgentType : Agent>(
    private val low: Int,
    private val high: Int,
    private val generator: () -> AgentType
) : BasicAgent(), AgentPool {
    private val agents = LinkedBlockingQueue<AgentType>()
    private val done = Semaphore(0)

    var agentCount = AtomicInteger(0)
    var processing = AtomicInteger(0)
    var enabled = AtomicBoolean(true)

    override fun <I : Information> inject(message: I): I = with(agents.poll() ?: makeOrWaitForAgent()) {
        processing.incrementAndGet()
        inject(message.apply { whenState(PROCESSED) {
            if (processing.getAndDecrement() >= agentCount.get() || agents.size < high || agents.size == 0) agents.put(this@with)
            if (!enabled.get() && processing.get() == 0) done.release()
        } })
    }

    fun close() {
        enabled.set(false)
    }

    override fun synchronize() {
        done.acquire()
    }

    private fun makeOrWaitForAgent(): AgentType = if (agentCount.get() < high) generate() else agents.take()
    private fun generate(): AgentType = generator.invoke().also { agentCount.incrementAndGet() }
}
