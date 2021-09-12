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
import com.coradec.coradeck.core.model.Priority.C2
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
        assertThat(r1.createdAt).isAfter(testee.createdAt)
        assertThat(testee.validFrom).isEqualTo(testee.createdAt)
        assertThat(r1.validFrom).isEqualTo(testee.validFrom)
        assertThat(testee.validUpTo).isEqualTo(ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC))
        assertThat(r1.validUpTo).isEqualTo(ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC))
        assertThat(testee.due).isEqualTo(testee.createdAt)
        assertThat(r1.due).isEqualTo(testee.due)
        assertThat(testee.priority).isEqualTo(A2)
        assertThat(r1.priority).isEqualTo(C2)
        assertThat(testee.new).isFalse()
        assertThat(r1.new).isTrue()
        assertThat(testee.enqueued).isTrue()
        assertThat(r1.enqueued).isFalse()
        assertThat(testee.dispatched).isTrue()
        assertThat(r1.dispatched).isFalse()
        assertThat(testee.delivered).isTrue()
        assertThat(r1.delivered).isFalse()
        assertThat(testee.processed).isTrue()
        assertThat(r1.processed).isFalse()
        assertThat(testee.observerCount).isEqualTo(0)
        assertThat(r1.observerCount).isEqualTo(testee.observerCount)
        assertThat(testee.deferred).isFalse()
        assertThat(r1.deferred).isEqualTo(testee.deferred)
        assertThat(r1.session).isEqualTo(testee.session)
        assertThat(testee.delayMs).isEqualTo(0)
        assertThat(r1.delayMs).isEqualTo(testee.delayMs)
        assertThat(testee.content).isEqualTo("BarrelCopy")
        assertThat(r1.content).isEqualTo("ParametrizedCopy")
    }

    class TestInformation(
        origin: Origin,
        priority: Priority,
        val content: String,
        createdAt: ZonedDateTime = now(),
        validFrom: ZonedDateTime = createdAt,
        validUpTo: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
    ) : BasicInformation(origin, priority, createdAt, Session.new, validFrom, validUpTo)

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