/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Information.Companion.LOST_ITEMS
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.State.LOST
import com.coradec.coradeck.com.model.impl.BasicMessage
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.Semaphore

internal class CIMMEXUT {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTypeImpl(), CoraComImpl(), CoraTextImpl(), CoraControlImpl())
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            CoraModules.initialize()
        }
    }

    @Test
    fun testDirectedMessageInjectionCIMMEX() {
        // given
        val agent = TestAgent1()
        val message = TestMessage1(here, agent)
        // when
        CIMMEX.inject(message)
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isTrue()
    }

    @Test
    fun testBroadcastMessageInjectionCIMMEX() {
        // given
        val agent = TestAgent1()
        val message = TestMessage1(here)
        CIMMEX.plugin(TestMessage1::class, agent)
        // when
        CIMMEX.inject(message)
        CIMMEX.synchronize()
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isTrue()
        assertThat(LOST_ITEMS).doesNotContain(message)
        // cleanup
        CIMMEX.unplug(agent)
    }

    @Test
    fun testLostBroadcastMessage() {
        // given
        val message = TestMessage1(here)
        // when
        CIMMEX.inject(message)
        CIMMEX.synchronize()
        // then
        assertThat(message.state).isEqualTo(LOST)
        assertThat(LOST_ITEMS).contains(message)
    }

    @Test
    fun testMessageInjectionAgent() {
        // given
        val agent = TestAgent1()
        val message = TestMessage1(here)
        // when
        agent.inject(message)
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isTrue()
    }

    @Test
    fun testDelayedMessageInjectionCIMMEX() {
        // given
        val then = System.currentTimeMillis()
        val delay = Duration.ofSeconds(2)
        val agent = TestAgent2()
        val message = TestMessage2(here, agent, delay)
        // when
        agent.inject(message)
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isFalse()
        message.standBy()
        assertThat(agent.gotMessage).isTrue()
        assertThat(System.currentTimeMillis()).isGreaterThan(then + delay.toMillis())
    }

    class TestMessage1(origin: Origin, target: Recipient? = null) : BasicMessage(origin, target = target) {
        override val copy get() = TestMessage1(origin, recipient)
        override fun copy(recipient: Recipient?) = TestMessage1(origin, recipient)
    }

    class TestMessage2(
        origin: Origin,
        target: Recipient? = null,
        delay: Duration
    ) : BasicMessage(origin, target = target, validFrom = ZonedDateTime.now().plus(delay)) {
        override val copy get() = TestMessage1(origin, recipient)
        override fun copy(recipient: Recipient?) = TestMessage1(origin, recipient)
        private val semaphore = Semaphore(0)
        fun standBy() {
            semaphore.acquire()
        }

        fun done() {
            semaphore.release()
        }
    }

    class TestAgent1 : BasicAgent() {
        var gotMessage: Boolean = false
        override fun onMessage(message: Information) = when (message) {
            is TestMessage1 -> gotMessage = true
            else -> super.onMessage(message)
        }
    }

    class TestAgent2 : BasicAgent() {
        var gotMessage: Boolean = false
        override fun onMessage(message: Information) = when (message) {
            is TestMessage2 -> {
                gotMessage = true
                message.done()
            }
            else -> super.onMessage(message)
        }
    }

}
