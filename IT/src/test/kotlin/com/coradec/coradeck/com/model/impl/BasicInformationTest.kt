/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.*
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.ZonedDateTime.now

internal class BasicInformationTest {
    private val IMMEX = CoraControl.IMMEX

    @Test fun bareCopyOfLost() {
        // given
        val testee = TestInformation(here, A2, "BarrelCopy")
        IMMEX.inject(testee)
        Thread.sleep(100)
        // when
        val r1: TestInformation = testee.copy()
        // then
        assertThat(r1.origin).isEqualTo(testee.origin)
        assertThat(r1.createdAt).isAfter(testee.createdAt)
        assertThat(r1.validFrom).isEqualTo(testee.validFrom)
        assertThat(testee.validFrom).isEqualTo(testee.createdAt)
        assertThat(r1.due).isEqualTo(testee.due)
        assertThat(testee.due).isEqualTo(testee.createdAt)
        assertThat(testee.priority).isEqualTo(A2)
        assertThat(r1.priority).isEqualTo(testee.priority)
        assertThat(testee.deferred).isFalse()
        assertThat(r1.deferred).isEqualTo(testee.deferred)
        assertThat(testee.validUpTo).isEqualTo(testee.validFrom + CoraCom.standardValidity)
        assertThat(r1.validUpTo).isEqualTo(r1.validFrom + CoraCom.standardValidity)
        assertThat(r1.session).isEqualTo(testee.session)
        assertThat(testee.delayMs).isEqualTo(0)
        assertThat(r1.delayMs).isEqualTo(testee.delayMs)
        assertThat(testee.content).isEqualTo("BarrelCopy")
        assertThat(r1.content).isEqualTo(testee.content)
    }

    @Test fun parametrizedCopyOfLost() {
        // given
        val testee = TestInformation(here, A2, "BarrelCopy")
        IMMEX.inject(testee)
        Thread.sleep(100)
        // when
        val r1: TestInformation = testee.copy(
            "priority" to C2,
            "content" to "ParametrizedCopy",
            "nonsense" to "Bullshit!"
        )
        val r2: TestInformation = testee.copy(mapOf(
            "priority" to B3,
            "content" to "AnotherParametrizedCopy",
            "origin" to "Bullshit!",
            "validFrom" to now().plusMinutes(2),
            "validUpTo" to now()
        ))
        // then
        assertThat(r1.origin).isEqualTo(testee.origin)
        assertThat(r2.origin).isEqualTo(r1.origin)
        assertThat(r1.createdAt).isAfter(testee.createdAt)
        assertThat(r2.createdAt).isAfter(r1.createdAt)
        assertThat(testee.validFrom).isEqualTo(testee.createdAt)
        assertThat(r1.validFrom).isEqualTo(testee.validFrom)
        assertThat(r2.validFrom).isBetween(now().plusMinutes(1), now().plusMinutes(2))
        assertThat(testee.validUpTo).isEqualTo(testee.validFrom + CoraCom.standardValidity)
        assertThat(r1.validUpTo).isEqualTo(r1.validFrom + CoraCom.standardValidity)
        assertThat(r2.validUpTo).isBefore(now())
        assertThat(testee.due).isEqualTo(testee.createdAt)
        assertThat(r1.due).isEqualTo(testee.due)
        assertThat(r2.due).isEqualTo(r2.validFrom)
        assertThat(testee.priority).isEqualTo(A2)
        assertThat(r1.priority).isEqualTo(C2)
        assertThat(r2.priority).isEqualTo(B3)
        assertThat(testee.deferred).isFalse()
        assertThat(r1.deferred).isEqualTo(testee.deferred)
        assertThat(r2.deferred).isTrue()
        assertThat(r1.session).isEqualTo(testee.session)
        assertThat(r2.session).isEqualTo(testee.session)
        assertThat(testee.delayMs).isEqualTo(0)
        assertThat(r1.delayMs).isEqualTo(testee.delayMs)
        assertThat(r2.delayMs).isGreaterThan(0L)
        assertThat(testee.content).isEqualTo("BarrelCopy")
        assertThat(r1.content).isEqualTo("ParametrizedCopy")
        assertThat(r2.content).isEqualTo("AnotherParametrizedCopy")
    }

    @Test fun parametrizedCopyOfDelivered() {
        // given
        val testee = TestInformation(here, A2, "BarrelCopy")
        val agent = TestAgent()
        agent.accept(testee)
        // when
        val r1: TestInformation = testee.copy(
            "priority" to C2,
            "content" to "ParametrizedCopy",
            "nonsense" to "Bullshit!"
        )
        val r2: TestInformation = testee.copy(mapOf(
            "priority" to B3,
            "content" to "AnotherParametrizedCopy",
            "origin" to "Bullshit!",
            "validFrom" to now().plusMinutes(2),
            "validUpTo" to now()
        ))
        // then
        assertThat(r1.origin).isEqualTo(testee.origin)
        assertThat(r2.origin).isEqualTo(r1.origin)
        assertThat(r1.createdAt).isAfter(testee.createdAt)
        assertThat(r2.createdAt).isAfter(r1.createdAt)
        assertThat(testee.validFrom).isEqualTo(testee.createdAt)
        assertThat(r1.validFrom).isEqualTo(testee.validFrom)
        assertThat(r2.validFrom).isBetween(now().plusMinutes(1), now().plusMinutes(2))
        assertThat(testee.validUpTo).isEqualTo(testee.validFrom + CoraCom.standardValidity)
        assertThat(r1.validUpTo).isEqualTo(r1.validFrom + CoraCom.standardValidity)
        assertThat(r2.validUpTo).isBefore(now())
        assertThat(testee.due).isEqualTo(testee.createdAt)
        assertThat(r1.due).isEqualTo(testee.due)
        assertThat(r2.due).isEqualTo(r2.validFrom)
        assertThat(testee.priority).isEqualTo(A2)
        assertThat(r1.priority).isEqualTo(C2)
        assertThat(r2.priority).isEqualTo(B3)
        assertThat(testee.deferred).isFalse()
        assertThat(r1.deferred).isEqualTo(testee.deferred)
        assertThat(r2.deferred).isTrue()
        assertThat(r1.session).isEqualTo(testee.session)
        assertThat(r2.session).isEqualTo(testee.session)
        assertThat(testee.delayMs).isEqualTo(0)
        assertThat(r1.delayMs).isEqualTo(testee.delayMs)
        assertThat(r2.delayMs).isGreaterThan(0L)
        assertThat(testee.content).isEqualTo("BarrelCopy")
        assertThat(r1.content).isEqualTo("ParametrizedCopy")
        assertThat(r2.content).isEqualTo("AnotherParametrizedCopy")
    }

    class TestInformation(
        origin: Origin,
        priority: Priority,
        val content: String,
        createdAt: ZonedDateTime = now(),
        validFrom: ZonedDateTime = createdAt,
        validUpTo: ZonedDateTime  = validFrom + CoraCom.standardValidity
    ) : BasicInformation(origin, priority, createdAt, Session.current, validFrom, validUpTo)

    class TestAgent: BasicAgent() {
        override fun receive(notification: Notification<*>) = when (val message = notification.content) {
            is Request -> message.succeed()
            is TestInformation -> relax()
            else -> super.receive(notification)
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic fun setup() {
            CoraModules.register(
                CoraConfImpl::class,
                CoraComImpl::class,
                CoraTextImpl::class,
                CoraTypeImpl::class,
                CoraControlImpl::class
            )
        }
    }
}
