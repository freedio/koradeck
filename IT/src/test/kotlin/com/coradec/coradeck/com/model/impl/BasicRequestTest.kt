package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.State.NEW
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Expiration.Companion.never_expires
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.trouble.BasicException
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
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
        softly.assertThat(request.urgent).isFalse()
        val finished = ZonedDateTime.now()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.reason).isNull()
        softly.assertThat(request.expires).isEqualTo(never_expires)
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
        val request = SuccessfulTestRequest(here, agent)
        // when:
        agent.inject(request).standBy()
        val finished = ZonedDateTime.now()
        // then:
        val softly = SoftAssertions()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.enqueued).isTrue()
        softly.assertThat(request.complete).isTrue()
        softly.assertThat(request.successful).isTrue()
        softly.assertThat(request.createdAt).isBetween(started, finished)
        softly.assertThat(request.cancelled).isFalse()
        softly.assertThat(request.failed).isFalse()
        softly.assertThat(request.reason).isNull()
        softly.assertAll()
    }

    @Test fun failedSimpleRequest() {
        // given:
        val started = ZonedDateTime.now()
        val agent = TestAgent()
        val request = FailedTestRequest(here, agent)
        // when:
        try {
            agent.inject(request).standBy()
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
        val request = CancelledTestRequest(here, agent)
        // when:
        try {
            agent.inject(request).standBy()
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
        val request = CancelledTestRequest2(here, agent)
        // when:
        try {
            agent.inject(request).standBy()
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

    class SuccessfulTestRequest(origin: Origin, recipient: Recipient) : BasicRequest(origin, target = recipient)
    class FailedTestRequest(origin: Origin, recipient: Recipient) : BasicRequest(origin, target = recipient)
    class CancelledTestRequest(origin: Origin, recipient: Recipient) : BasicRequest(origin, target = recipient)
    class CancelledTestRequest2(origin: Origin, recipient: Recipient) : BasicRequest(origin, target = recipient)
    class TestFailureException: BasicException()
    class CancelReason : BasicException()

    class TestAgent(): BasicAgent() {
        override fun onMessage(message: Information) = when(message) {
            is SuccessfulTestRequest -> message.succeed()
            is FailedTestRequest -> message.fail(TestFailureException())
            is CancelledTestRequest -> message.cancel()
            is CancelledTestRequest2 -> message.cancel(CancelReason())
            else -> super.onMessage(message)
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraComImpl(), CoraTextImpl(), CoraTypeImpl(), CoraControlImpl())
        }
    }
}