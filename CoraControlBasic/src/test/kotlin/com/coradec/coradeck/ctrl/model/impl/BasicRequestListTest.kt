package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class BasicRequestListTest {

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl())
        }

        @AfterAll
        @JvmStatic fun teardown() {
            CoraModules.initialize()
        }
    }

    @Test fun testEmptyList() {
        // given
        val agent = TestAgent()
        val testee = BasicRequestList(here, listOf(), agent)
        // when
        agent.inject(testee).standBy()
        // then
        Thread.sleep(100)
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum).isEqualTo(0)
    }

    @Test
    fun testSuccessfulList() {
        // given:
        val agent = TestAgent()
        val req1 = TestRequest(agent, 100)
        val req2 = TestRequest(agent, 10)
        val req3 = TestRequest(agent, 1)
        val testee = BasicRequestList(here, listOf(req1, req2, req3))
        // when:
        agent.inject(testee).standBy()
        // then:
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum).isEqualTo(111)
        Thread.yield()
        assertThat(req1.observerCount).isEqualTo(0)
        assertThat(req2.observerCount).isEqualTo(0)
        assertThat(req3.observerCount).isEqualTo(0)
    }

    @Test
    fun testFailedList() {
        // given:
        val agent = TestAgent()
        val req1 = TestRequest(agent, 100)
        val req2 = FailingRequest(agent, 10)
        val req3 = TestRequest(agent, 1)
        val testee = BasicRequestList(here, listOf(req1, req2, req3))
        // when:
        val trouble = try {
            agent.inject(testee).standBy()
            null
        } catch (e: Exception) {
            e
        }
        // then:
        assertThat(testee.successful).isFalse()
        assertThat(testee.failed).isTrue()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum).isEqualTo(100)
        assertThat(trouble).isNotNull()
        Thread.sleep(100)
        assertThat(req1.observerCount).isEqualTo(0)
        assertThat(req2.observerCount).isEqualTo(0)
        assertThat(req3.observerCount).isEqualTo(0)
    }

    @Test
    fun testSingle() {
        // given:
        val agent = TestAgent()
        val req1 = TestRequest(agent, 100)
        val testee = BasicRequestList(here, listOf(req1))
        // when:
        agent.inject(testee).standBy()
        // then:
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum).isEqualTo(100)
        Thread.sleep(100)
        assertThat(req1.observerCount).isEqualTo(0)
    }

    @Test
    fun testCancelledList() {
        // given:
        val agent = TestAgent()
        val req1 = TestRequest(agent, 100)
        val req2 = CancellingRequest(agent, 10)
        val req3 = TestRequest(agent, 1)
        val testee = BasicRequestList(here, listOf(req1, req2, req3))
        // when:
        val trouble = try {
            agent.inject(testee).standBy()
            null
        } catch (e: Exception) {
            e
        }
        // then:
        assertThat(testee.successful).isFalse()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isTrue()
        assertThat(agent.sum).isEqualTo(100)
        assertThat(trouble).isNotNull()
        Thread.sleep(100)
        assertThat(req1.observerCount).isEqualTo(0)
        assertThat(req2.observerCount).isEqualTo(0)
        assertThat(req3.observerCount).isEqualTo(0)
    }


    @Test
    fun testRandomness() {
        // given:
        val agent = TestAgent2()
        val req1 = TestRequest(agent, 'a'.code)
        val req2 = TestRequest(agent, 'b'.code)
        val req3 = TestRequest(agent, 'c'.code)
        val req4 = TestRequest(agent, 'd'.code)
        val req5 = TestRequest(agent, 'e'.code)
        val req6 = TestRequest(agent, 'f'.code)
        val req7 = TestRequest(agent, 'g'.code)
        val req8 = TestRequest(agent, 'h'.code)
        val req9 = TestRequest(agent, 'i'.code)
        val testee = BasicRequestList(here, listOf(req1, req2, req3, req4, req5, req6, req7, req8, req9))
        // when:
        agent.inject(testee).standBy()
        // then:
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.value).isEqualTo("abcdefghi")
        Thread.yield()
        assertThat(req1.observerCount).isEqualTo(0)
        assertThat(req2.observerCount).isEqualTo(0)
        assertThat(req3.observerCount).isEqualTo(0)
        assertThat(req4.observerCount).isEqualTo(0)
        assertThat(req5.observerCount).isEqualTo(0)
        assertThat(req6.observerCount).isEqualTo(0)
        assertThat(req7.observerCount).isEqualTo(0)
        assertThat(req8.observerCount).isEqualTo(0)
        assertThat(req9.observerCount).isEqualTo(0)
    }

    class TestRequest(val agent: Agent, val value: Int, target: Recipient? = agent): BasicRequest(here, target = target)
    class FailingRequest(val agent: Agent, val value: Int, target: Recipient? = agent): BasicRequest(here, target = target)
    class CancellingRequest(val agent: Agent, val value: Int, target: Recipient? = agent): BasicRequest(here, target = target)

    class TestAgent : BasicAgent() {
        var sum = 0

        override fun onMessage(message: Information) = when (message) {
            is TestRequest -> {
                sum += message.value
                message.succeed()
            }
            is CancellingRequest -> message.cancel()
            is FailingRequest -> message.fail(RuntimeException("This was to be expected!"))
            else -> super.onMessage(message)
        }
    }

    class TestAgent2 : BasicAgent() {
        private var collector= StringBuilder()
        val value get() = collector.toString()

        override fun onMessage(message: Information) = when (message) {
            is TestRequest -> {
                collector.append(message.value.toChar())
                message.succeed()
            }
            is CancellingRequest -> message.cancel()
            is FailingRequest -> message.fail(RuntimeException("This was to be expected!"))
            else -> super.onMessage(message)
        }
    }
}