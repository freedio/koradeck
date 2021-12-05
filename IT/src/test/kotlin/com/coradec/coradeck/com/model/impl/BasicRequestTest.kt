/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.RequestState.NEW
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.trouble.BasicException
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

internal class BasicRequestTest {

    @Test fun newSimpleRequest() {
        // given:
        val started = ZonedDateTime.now()
        val request = BasicRequest(here)
        // when:
        // then:
        val softly = SoftAssertions()
        softly.assertThat(request.new).isTrue()
        softly.assertThat(request.enqueued).isFalse()
        softly.assertThat(request.dispatched).isFalse()
        softly.assertThat(request.complete).isFalse()
        softly.assertThat(request.successful).isFalse()
        softly.assertThat(request.failed).isFalse()
        softly.assertThat(request.cancelled).isFalse()
        softly.assertThat(request.priority).isEqualTo(defaultPriority)
        val finished = ZonedDateTime.now()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.reason).isNull()
        softly.assertThat(request.validFrom).isEqualTo(request.createdAt)
        softly.assertThat(request.validUpTo).isEqualTo(ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC))
        softly.assertThat(request.observerCount).isEqualTo(0)
        softly.assertThat(request.state).isEqualTo(NEW)
        softly.assertThat(request.states).containsExactly(NEW)
        softly.assertThat(request.origin).isInstanceOf(StackFrame::class.java)
        softly.assertThat((request.origin as StackFrame).className).isEqualTo(BasicRequestTest::class.classname)
        softly.assertThat((request.origin as StackFrame).methodName).isEqualTo("newSimpleRequest")
        softly.assertAll()
    }

    @Test fun successfulSimpleRequest() {
        // given:
        val started = ZonedDateTime.now()
        val agent = TestAgent()
        val request = SuccessfulTestRequest(here)
        // when:
        agent.accept(request).standby()
        val finished = ZonedDateTime.now()
        // then:
        val softly = SoftAssertions()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.dispatched).isTrue()
        softly.assertThat(request.complete).isTrue()
        softly.assertThat(request.successful).isTrue()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.cancelled).isFalse()
        softly.assertThat(request.failed).isFalse()
        softly.assertThat(request.reason).isNull()
        softly.assertAll()
    }

    @Test fun unqualifiedInjection() {
        // given:
        val started = ZonedDateTime.now()
        val agent = TestAgent()
        val request = SuccessfulTestRequest(here)
        // when:
        agent.accept(request).standby()
        val finished = ZonedDateTime.now()
        // then:
        val softly = SoftAssertions()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.dispatched).isTrue()
        softly.assertThat(request.complete).isTrue()
        softly.assertThat(request.successful).isTrue()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.cancelled).isFalse()
        softly.assertThat(request.failed).isFalse()
        softly.assertThat(request.reason).isNull()
        softly.assertAll()
    }

    @Test fun reinjectionOfUnqualified() {
        // given:
        val started = ZonedDateTime.now()
        val agent = TestAgent()
        val request = SuccessfulTestRequest(here)
        val agent2 = TestAgent()
        // when:
        val r1 = try {
            agent.accept(request).standby()
        } catch (e: Exception) {
            e
        }
        val r2 = try {
            agent2.accept(request).standby()
        } catch (e: Exception) {
            e
        }
        val finished = ZonedDateTime.now()
        // then:
        Thread.sleep(10)
        val softly = SoftAssertions()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.dispatched).isTrue()
        softly.assertThat(request.complete).isTrue()
        softly.assertThat(request.successful).isTrue()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.cancelled).isFalse()
        softly.assertThat(request.failed).isFalse()
        softly.assertThat(request.reason).isNull()
        softly.assertThat(r1).isInstanceOf(Message::class.java)
        softly.assertThat(r2).isInstanceOf(Message::class.java)
        softly.assertAll()
    }

    @Test fun failedSimpleRequest() {
        // given:
        val started = ZonedDateTime.now()
        val agent = TestAgent()
        val request = FailedTestRequest(here)
        // when:
        try {
            agent.accept(request).standby()
        } catch (e: Exception) {
        }
        val finished = ZonedDateTime.now()
        // then:
        val softly = SoftAssertions()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.complete).isTrue()
        softly.assertThat(request.successful).isFalse()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.cancelled).isFalse()
        softly.assertThat(request.failed).isTrue()
        softly.assertThat(request.reason).isInstanceOf(TestFailureException::class.java)
        softly.assertAll()
    }

    @Test fun cancelledSimpleRequest() {
        // given:
        val started = ZonedDateTime.now()
        val agent = TestAgent()
        val request = CancelledTestRequest(here)
        // when:
        try {
            agent.accept(request).standby()
        } catch (e: Exception) {
        }
        val finished = ZonedDateTime.now()
        // then:
        val softly = SoftAssertions()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.complete).isTrue()
        softly.assertThat(request.successful).isFalse()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.cancelled).isTrue()
        softly.assertThat(request.failed).isFalse()
        softly.assertThat(request.reason).isNull()
        softly.assertAll()
    }

    @Test fun cancelledSimpleRequestWithReason() {
        // given:
        val started = ZonedDateTime.now()
        val agent = TestAgent()
        val request = CancelledTestRequest2(here)
        // when:
        try {
            agent.accept(request).standby()
        } catch (e: Exception) {
        }
        val finished = ZonedDateTime.now()
        // then:
        val softly = SoftAssertions()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.complete).isTrue()
        softly.assertThat(request.successful).isFalse()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.cancelled).isTrue()
        softly.assertThat(request.failed).isFalse()
        softly.assertThat(request.reason).isInstanceOf(CancelReason::class.java)
        softly.assertAll()
    }

    class SuccessfulTestRequest(origin: Origin) : BasicRequest(origin)
    class FailedTestRequest(origin: Origin) : BasicRequest(origin)
    class CancelledTestRequest(origin: Origin) : BasicRequest(origin)
    class CancelledTestRequest2(origin: Origin) : BasicRequest(origin)
    class TestFailureException: BasicException()
    class CancelReason : BasicException()
    class TestAgent : BasicAgent() {

        override fun accepts(notification: Notification<*>) = when (notification.content) {
            is SuccessfulTestRequest, is FailedTestRequest, is CancelledTestRequest, is CancelledTestRequest2 -> true
            else -> super.accepts(notification)
        }

        override fun receive(notification: Notification<*>) = when(val message = notification.content) {
            is SuccessfulTestRequest -> message.succeed()
            is FailedTestRequest -> message.fail(TestFailureException())
            is CancelledTestRequest -> message.cancel()
            is CancelledTestRequest2 -> message.cancel(CancelReason())
            else -> super.receive(notification)
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraComImpl(), CoraTextImpl(), CoraTypeImpl(), CoraControlImpl())
        }
    }
}
