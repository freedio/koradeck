/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.impl.BasicInformation
import com.coradec.coradeck.com.model.impl.BasicMessage
import com.coradec.coradeck.com.model.impl.BasicNotification
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.core.util.swallow
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

internal class BasicItemListTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl())
        }
    }

    @Test
    fun testEmptyList() {
        // given
        val agent = TestAgent()
        val testee = BasicItemList(here, emptySequence())
        // when
        agent.accept(testee).standby()
        // then
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum.get()).isEqualTo(0)
    }

    @Test
    fun testSuccessfulList() {
        // given:
        val agent = TestAgent()
        val item1 = TestRequest(1000)
        val item2 = TestInformation(100)
        val item3 = TestNotification(10)
        val item4 = TestMessage(1, agent)
        val testee = BasicItemList(here, sequenceOf(item1, item2, item3, item4), processor = agent)
        // when:
        agent.accept(testee).content.standby()
        // then:
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum.get()).isEqualTo(1111)
        assertThat(item1.observerCount).isEqualTo(0)
        assertThat(item3.observerCount).isEqualTo(0)
        assertThat(item4.observerCount).isEqualTo(0)
    }

    @Test
    fun testSuccessfulListViaModule() {
        // given:
        val agent = TestAgent()
        val item1 = TestRequest(1000)
        val item2 = TestInformation(100)
        val item3 = TestNotification(10)
        val item4 = TestMessage(1, agent)
        val testee = CoraControl.createItemList(here, item1, item2, item3, item4, processor = agent)
        // when:
        agent.accept(testee).standby()
        // then:
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum.get()).isEqualTo(1111)
        Thread.sleep(10)
        assertThat(item1.observerCount).isEqualTo(0)
        assertThat(item3.observerCount).isEqualTo(0)
        assertThat(item4.observerCount).isEqualTo(0)
    }

    @Test
    fun testFailedList() {
        // given:
        val agent = TestAgent()
        val item1 = TestRequest(1000)
        val item2 = FailingRequest(100)
        val item3 = TestInformation(10)
        val item4 = TestMessage(1, agent)
        val testee = BasicItemList(here, sequenceOf(item1, item2, item3, item4), processor = agent)
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
        assertThat(testee.failed).isTrue()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum.get()).isEqualTo(1000)
        assertThat(trouble).isNotNull()
        Thread.sleep(10)
        assertThat(item1.observerCount).isEqualTo(0)
        assertThat(item2.observerCount).isEqualTo(0)
        assertThat(item4.observerCount).isEqualTo(0)
    }

    @Test
    fun testFailedListDueToLostInformation() {
        // given:
        val agent = TestAgent()
        val item1 = TestRequest(1000)
        val item2 = TestRequest(100)
        val item3 = LostInformation(10)
        val item4 = TestMessage(1, agent)
        val testee = BasicItemList(here, sequenceOf(item1, item2, item3, item4).asSequence(), processor = agent)
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
        assertThat(agent.sum.get()).isEqualTo(1100)
        assertThat(trouble).isNotNull()
        Thread.sleep(10)
        assertThat(item1.observerCount).isEqualTo(0)
        assertThat(item2.observerCount).isEqualTo(0)
        assertThat(item4.observerCount).isEqualTo(0)
    }

    @Test
    fun testSingle() {
        // given:
        val agent = TestAgent()
        val item1 = TestInformation(100)
        val testee = BasicItemList(here, sequenceOf(item1).asSequence(), processor = agent)
        // when:
        val message = agent.accept(testee)
        message.standby()
        // then:
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.sum.get()).isEqualTo(100)
        assertThat(message.state).isEqualTo(NotificationState.PROCESSED)
    }

    @Test
    fun testCancelledList() {
        // given:
        val agent = TestAgent()
        val req1 = TestRequest(100)
        val req2 = CancellingRequest(10)
        val req3 = TestRequest(1)
        val testee = BasicItemList(here, sequenceOf(req1, req2, req3).asSequence(), processor = agent)
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
        assertThat(agent.sum.get()).isEqualTo(100)
        assertThat(trouble).isNotNull()
        Thread.sleep(10)
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
        val testee = BasicItemList(here, sequenceOf(req1, req2, req3, req4, req5, req6, req7, req8, req9), processor = agent)
        // when:
        agent.accept(testee).standby()
        // then:
        Thread.yield()
        assertThat(testee.successful).isTrue()
        assertThat(testee.failed).isFalse()
        assertThat(testee.cancelled).isFalse()
        assertThat(agent.value).isEqualTo("abcdefghi")
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

    class TestRequest(val value: Int) : BasicRequest(here)
    class TestInformation(val value: Int) : BasicInformation(here)
    class LostInformation(val value: Int) : BasicInformation(here)
    class TestNotification(val value: Int) : BasicNotification<Information>(TestInformation(value))
    class TestMessage(val value: Int, recipient: Recipient) : BasicMessage<Request>(TestRequest(value), recipient)

    class FailingRequest(val value: Int) : BasicRequest(here)
    class CancellingRequest(val value: Int) : BasicRequest(here)

    class TestAgent : BasicAgent() {
        val sum = AtomicInteger(0)

        @Suppress("UNCHECKED_CAST")
        override fun receive(notification: Notification<*>) = when (val message = notification.content) {
            is TestRequest -> sum.addAndGet(message.value).also { message.succeed() }.swallow()
            is TestMessage -> sum.addAndGet((message as Message<TestRequest>).content.value).swallow()
            is TestInformation -> sum.addAndGet(message.value).swallow()
            is TestNotification -> sum.addAndGet((message as Notification<TestInformation>).content.value).swallow()
            is CancellingRequest -> message.cancel()
            is FailingRequest -> message.fail(RuntimeException("This was to be expected!"))
            else -> super.receive(notification)
        }
    }

    class TestAgent2 : BasicAgent() {
        private var collector = StringBuffer()
        val value get() = collector.toString()

        override fun receive(notification: Notification<*>) = when (val message = notification.content) {
            is TestRequest -> {
                if (!message.cancelled) {
                    synchronized(collector) { collector.append(message.value.toChar()) }
                    message.succeed()
                }
                relax()
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
