package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.State.PROCESSED
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.A2
import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.module.CoraModules
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

internal class BasicMessageTest {

    @Test fun injectionThroughAgent() {
        // given
        val testee = TestMessage(here, A2, "BarrelCopy")
        val agent = TestAgent()
        // when
        val r0 = testee.recipient
        val r1 = agent.inject(testee)
        Thread.sleep(100)
        // then
        assertThat(r0).isNull()
        assertThat(r1).isSameAs(testee)
        assertThat(testee.recipient).isEqualTo(agent)
        assertThat(testee.state).isEqualTo(PROCESSED)
        assertThat(testee.validFrom).isEqualTo(testee.createdAt)
        assertThat(testee.validUpTo).isEqualTo(ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC))
        assertThat(testee.due).isEqualTo(testee.createdAt)
        assertThat(testee.priority).isEqualTo(A2)
        assertThat(testee.new).isFalse()
        assertThat(testee.enqueued).isTrue()
        assertThat(testee.dispatched).isTrue()
        assertThat(testee.delivered).isTrue()
        assertThat(testee.processed).isTrue()
        assertThat(testee.observerCount).isEqualTo(0)
        assertThat(testee.deferred).isFalse()
        assertThat(testee.delayMs).isEqualTo(0)
        assertThat(testee.content).isEqualTo("BarrelCopy")
    }

    @Test fun reinjectionThroughAgent() {
        // given
        val testee = TestMessage(here, A2, "BarrelCopy")
        val agent = TestAgent()
        // when
        val r1 = agent.inject(testee)
        val r2 = agent.inject(r1)
        Thread.sleep(100)
        // then
        assertThat(r1).isSameAs(testee)
        assertThat(r2).isNotSameAs(testee)
        assertThat(r1.state).isEqualTo(PROCESSED)
        assertThat(testee.state).isEqualTo(PROCESSED)
        assertThat(r1.origin).isEqualTo(testee.origin)
        assertThat(r2.createdAt).isAfter(testee.createdAt)
        assertThat(testee.validFrom).isEqualTo(testee.createdAt)
        assertThat(r2.validFrom).isEqualTo(testee.validFrom)
        assertThat(testee.validUpTo).isEqualTo(ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC))
        assertThat(r2.validUpTo).isEqualTo(ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC))
        assertThat(testee.due).isEqualTo(testee.createdAt)
        assertThat(r2.due).isEqualTo(testee.due)
        assertThat(testee.priority).isEqualTo(A2)
        assertThat(r2.priority).isEqualTo(A2)
        assertThat(testee.new).isFalse()
        assertThat(r2.new).isFalse()
        assertThat(testee.enqueued).isTrue()
        assertThat(r2.enqueued).isTrue()
        assertThat(testee.dispatched).isTrue()
        assertThat(r2.dispatched).isTrue()
        assertThat(testee.delivered).isTrue()
        assertThat(r2.delivered).isTrue()
        assertThat(testee.processed).isTrue()
        assertThat(r2.processed).isTrue()
        assertThat(testee.observerCount).isEqualTo(0)
        assertThat(r2.observerCount).isEqualTo(testee.observerCount)
        assertThat(testee.deferred).isFalse()
        assertThat(r2.deferred).isEqualTo(testee.deferred)
        assertThat(r2.session).isEqualTo(testee.session)
        assertThat(testee.delayMs).isEqualTo(0)
        assertThat(r2.delayMs).isEqualTo(testee.delayMs)
        assertThat(testee.content).isEqualTo("BarrelCopy")
        assertThat(r2.content).isEqualTo("BarrelCopy")
    }

    @Test fun testFormatted() {
        // given
        val testee = TestMessage(here, A2, "Hello, world!")
        // when
        val r1 = testee.formatted
        // then
        assertThat(r1).startsWith("TestMessage([")
        assertThat(r1).contains("content: \"Hello, world!\"")
        assertThat(r1).contains("priority: A2")
    }

    class TestMessage(
        origin: Origin,
        priority: Priority,
        val content: String,
        createdAt: ZonedDateTime = now(),
        validFrom: ZonedDateTime = createdAt,
        validUpTo: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC),
        target: Recipient? = null
    ) : BasicMessage(origin, priority, createdAt, Session.new, target, validFrom,validUpTo)

    class TestAgent: BasicAgent() {
        override fun onMessage(message: Information) {
            if (message is Request) message.succeed()
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(CoraConfImpl(), CoraComImpl(), CoraTextImpl(), CoraTypeImpl(), CoraControlImpl())
        }
    }
}