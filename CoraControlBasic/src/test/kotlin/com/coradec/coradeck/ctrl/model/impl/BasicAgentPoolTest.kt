/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.impl.BasicMessage
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

internal class BasicAgentPoolTest {
    val IMMEX = CoraControl.IMMEX

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl())
        }
    }

    @Test fun testEasySmallPool() {
        // given:
        IMMEX.synchronize()
        val evaluation = TestEvaluation()
        val testee = BasicAgentPool(0, 3) { TestAgent(evaluation) }
        // when:
        IntRange(0, 1000).forEach { _ -> testee.inject(TestMessage(here)) }
        testee.close()
        testee.synchronize()
        // then:
        assertThat(evaluation.messages).hasSize(3)
    }

    @Test fun testHeavyMediumPool() {
        // given:
        IMMEX.synchronize()
        val evaluation = TestEvaluation()
        val testee = BasicAgentPool(0, 10) { TestAgent(evaluation) }
        // when:
        IntRange(0, 10000).forEach { _ -> testee.inject(TestMessage(here)) }
        testee.close()
        testee.synchronize()
        // then:
        assertThat(evaluation.messages).hasSize(10)
    }

    @Test fun testHeavyBigPool() {
        // given:
        IMMEX.synchronize()
        val evaluation = TestEvaluation()
        val testee = BasicAgentPool(0, 2000) { TestAgent(evaluation) }
        // when:
        IntRange(0, 20000).forEach { _ -> testee.inject(TestMessage(here)) }
        testee.close()
        testee.synchronize()
        // then:
        assertThat(evaluation.messages).hasSize(2000)
    }

    class TestEvaluation {
        val messages = ConcurrentHashMap<Agent, AtomicInteger>()

        fun add(agent: Agent) {
            messages.computeIfAbsent(agent) { AtomicInteger(0) }.incrementAndGet()
        }

    }

    class TestAgent(val evaluation: TestEvaluation) : BasicAgent() {
        override fun onMessage(message: Information) = when (message) {
            is TestMessage -> {
                evaluation.add(this)
                Thread.sleep(1)
            }
            else -> super.onMessage(message)
        }
    }

    class TestMessage(origin: Origin) : BasicMessage(origin)
}