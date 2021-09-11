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
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class BasicAgentPool<AgentType : Agent>(
    private val low: Int,
    private val high: Int,
    private val generator: () -> AgentType
) : BasicAgent(), AgentPool {
    private val agents = LinkedBlockingQueue<AgentType>()
    private val done = Semaphore(0)
    private val agentCount = AtomicInteger(0)
    private val processing = AtomicInteger(0)
    private val enabled = AtomicBoolean(true)
    private var used = 0
    private var submitted = AtomicLong(0L)
    private var processed = AtomicLong(0L)

    override val stats: String get() = "Low: $low, High: $high, Agents Used: $used, Messages submitted: $submitted & processed: $processed"

    override fun <I : Information> inject(message: I): I = with(agents.poll() ?: makeOrWaitForAgent()) {
        used = max(used, processing.incrementAndGet())
        submitted.incrementAndGet()
        inject(message.apply { whenState(PROCESSED) {
            processed.incrementAndGet()
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
