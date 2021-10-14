/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.State.PROCESSED
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.trouble.StandbyTimeoutException
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.model.AgentPool
import com.coradec.coradeck.ctrl.module.CoraControl
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit.SECONDS
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
    private val messages = CopyOnWriteArraySet<Message<*>>()
    private val done = Semaphore(0)
    private val agentCount = AtomicInteger(0)
    private val processing = AtomicInteger(0)
    private val enabled = AtomicBoolean(true)
    private var used = 0
    private var submitted = AtomicLong(0L)
    private var digested = AtomicLong(0L)

    override val stats: String get() = "Low: $low, High: $high, Agents Used: $used, Messages submitted: $submitted, processing: $processing(${messages.size}) & processed: $digested; state: ${if (enabled.get()) "enabled" else "disabled"}"

    override fun <I : Information> accept(info: I): Message<I> = with(agents.poll() ?: makeOrWaitForAgent()) {
        used = max(used, processing.incrementAndGet())
        submitted.incrementAndGet()
        accept(info).apply {
            whenState(PROCESSED) {
                messages -= this
                digested.incrementAndGet()
                if (processing.getAndDecrement() >= agentCount.get() || agents.size < high || agents.size == 0) agents.put(this@with)
            }
        }.also { messages += it }
    }

    override fun shutdown() {
        enabled.set(false)
        synchronize()
    }

    override fun synchronize() {
        debug("Synchronizing...")
        val IMMEX = CoraControl.IMMEX
        var maxRetries = 10
        while (!done.tryAcquire(1, SECONDS) && --maxRetries > 0) {
            debug("Statistics: «%s»", stats)
            debug("CIMMEX Stats: %s", IMMEX.stats)
            tryRelease()
        }
        if (maxRetries == 0) {
            debug("Unprocessed: %s", messages)
            throw StandbyTimeoutException(Timespan(10, SECONDS))
        }
        debug("Synchronized.")
    }

    private fun tryRelease() {
        if (!enabled.get() && processing.get() == 0) done.release()
    }

    private fun makeOrWaitForAgent(): AgentType = if (agentCount.get() < high) generate() else agents.take()
    private fun generate(): AgentType = generator.invoke().also { agentCount.incrementAndGet() }
}
