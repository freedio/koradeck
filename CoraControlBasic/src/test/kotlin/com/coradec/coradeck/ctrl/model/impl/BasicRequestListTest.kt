/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

internal class BasicRequestListTest {

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl())
        }
    }

    @Test fun testEmptyList() {
        // given
        val agent = TestAgent()
        val testee = BasicRequestList(here, emptySequence())
        // when
        agent.accept(testee).standby()
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
        val agent = BasicItemListTest.TestAgent()
        val req1 = BasicItemListTest.TestRequest(100)
        val req2 = BasicItemListTest.TestRequest(10)
        val req3 = BasicItemListTest.TestRequest(1)
        val testee = BasicItemList(here, sequenceOf(req1, req2, req3), processor = agent)
        // when:
        agent.accept(testee).standby()
        // then:
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum).isEqualTo(111)
        Thread.sleep(10)
        assertThat(req1.observerCount).isEqualTo(0)
        assertThat(req2.observerCount).isEqualTo(0)
        assertThat(req3.observerCount).isEqualTo(0)
    }

    @Test
    fun testFailedList() {
        // given:
        val agent = TestAgent()
        val req1 = TestRequest(100)
        val req2 = FailingRequest(10)
        val req3 = TestRequest(1)
        val testee = BasicRequestList(here, sequenceOf(req1, req2, req3), processor = agent)
        // when:
        val trouble = try {
            agent.accept(testee).standby()
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
        val req1 = TestRequest(100)
        val testee = BasicRequestList(here, sequenceOf(req1), processor = agent)
        // when:
        agent.accept(testee).standby()
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
        val req1 = TestRequest(100)
        val req2 = CancellingRequest(10)
        val req3 = TestRequest(1)
        val testee = BasicRequestList(here, sequenceOf(req1, req2, req3), processor = agent)
        // when:
        val trouble = try {
            agent.accept(testee).standby()
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
        val req1 = TestRequest('a'.code)
        val req2 = TestRequest('b'.code)
        val req3 = TestRequest('c'.code)
        val req4 = TestRequest('d'.code)
        val req5 = TestRequest('e'.code)
        val req6 = TestRequest('f'.code)
        val req7 = TestRequest('g'.code)
        val req8 = TestRequest('h'.code)
        val req9 = TestRequest('i'.code)
        val testee = BasicRequestList(here, sequenceOf(req1, req2, req3, req4, req5, req6, req7, req8, req9), processor = agent)
        // when:
        agent.accept(testee).standby()
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

    class TestRequest(val value: Int): BasicRequest(here)
    class FailingRequest(val value: Int): BasicRequest(here)
    class CancellingRequest(val value: Int): BasicRequest(here)

    class TestAgent : BasicAgent() {
        var sum = 0

        override fun receive(notification: Notification<*>) = when (val message = notification.content) {
            is TestRequest -> {
                sum += message.value
                message.succeed()
            }
            is CancellingRequest -> {
                message.cancel()
            }
            is FailingRequest -> {
                message.fail(RuntimeException("This was to be expected!"))
            }
            else -> super.receive(notification)
        }
    }

    class TestAgent2 : BasicAgent() {
        private var collector= StringBuilder()
        val value get() = collector.toString()

        override fun receive(notification: Notification<*>) = when (val message = notification.content) {
            is TestRequest -> {
                collector.append(message.value.toChar())
                message.succeed()
            }
            is CancellingRequest -> {
                message.cancel()
            }
            is FailingRequest -> {
                message.fail(RuntimeException("This was to be expected!"))
            }
            else -> super.receive(notification)
        }
    }
}
