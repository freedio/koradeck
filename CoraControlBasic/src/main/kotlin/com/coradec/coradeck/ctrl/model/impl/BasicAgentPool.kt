/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.State.PROCESSED
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.model.AgentPool
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
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
    private var digested = AtomicLong(0L)

    override val stats: String get() = "Low: $low, High: $high, Agents Used: $used, Messages submitted: $submitted, processing: $processing & processed: $digested; state: ${if (enabled.get()) "enabled" else "disabled"}"

    override fun <M : Message> inject(message: M): M = with(agents.poll() ?: makeOrWaitForAgent()) {
        used = max(used, processing.incrementAndGet())
        submitted.incrementAndGet()
        inject(message.apply {
            whenState(PROCESSED) {
                digested.incrementAndGet()
                if (processing.getAndDecrement() >= agentCount.get() || agents.size < high || agents.size == 0) agents.put(this@with)
                tryRelease()
            }
        })
    }

    override fun shutdown() {
        enabled.set(false)
        synchronize()
    }

    override fun synchronize() {
        debug("Synchronizing...")
        while (!done.tryAcquire(1, TimeUnit.SECONDS)) {
            debug("Statistics: «%s»", stats)
            tryRelease()
        }
        debug("Synchronized.")
    }

    private fun tryRelease() {
        if (!enabled.get() && processing.get() == 0) done.release()
    }

    private fun makeOrWaitForAgent(): AgentType = if (agentCount.get() < high) generate() else agents.take()
    private fun generate(): AgentType = generator.invoke().also { agentCount.incrementAndGet() }
}
