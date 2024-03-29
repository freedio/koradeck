/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.NotificationState.PROCESSED
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.A2
import com.coradec.coradeck.core.model.Priority.B2
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.ctrl.trouble.NotificationAlreadyEnqueuedException
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BasicMessageTest {

    @Test
    fun injectionThroughAgent() {
        // given
        val agent = TestAgent()
        val info = TestInformation(here, A2)
        // when
        val testee: Message<TestInformation> = agent.accept(info)
        testee.standby()
        // then
        assertThat(testee.recipient).isEqualTo(agent)
        assertThat(testee.state).`as`("${testee.states} does not end with ‹PROCESSED›").isEqualTo(PROCESSED)
        assertThat(testee.validFrom).isEqualTo(info.validFrom)
        assertThat(testee.validUpTo).isEqualTo(info.validFrom + CoraCom.standardValidity)
        assertThat(testee.due).isEqualTo(testee.validFrom)
        assertThat(testee.priority).isEqualTo(A2)
        assertThat(testee.new).isFalse()
        assertThat(testee.enqueued).isTrue()
        assertThat(testee.dispatched).isTrue()
        assertThat(testee.delivered).isTrue()
        assertThat(testee.processed).isTrue()
        assertThat(testee.observerCount).isEqualTo(0)
        assertThat(testee.deferred).isFalse()
        assertThat(testee.delayMs).isEqualTo(0)
        assertThat(testee.content).isInstanceOf(TestInformation::class.java)
    }

    @Test
    fun reinjectionThroughIMMEX() {
        // given
        val agent = TestAgent()
        val info = TestInformation(here, A2)
        // when
        val r1 = agent.accept(info).standby()
        val r2 = try {
            IMMEX.inject(r1)
        } catch (e: Exception) {
            e
        }
        // then
        assertThat(r2).isInstanceOf(NotificationAlreadyEnqueuedException::class.java)
        assertThat(r1.state).`as`("${r1.states} does not end with ‹PROCESSED›").isEqualTo(PROCESSED)
        assertThat(r1.validFrom).isEqualTo(info.validFrom)
        assertThat(r1.validUpTo).isEqualTo(info.validFrom + CoraCom.standardValidity)
        assertThat(r1.due).isEqualTo(r1.validFrom)
        assertThat(r1.priority).isEqualTo(A2)
        assertThat(r1.new).isFalse()
        assertThat(r1.enqueued).isTrue()
        assertThat(r1.dispatched).isTrue()
        assertThat(r1.delivered).isTrue()
        assertThat(r1.processed).isTrue()
        assertThat(r1.observerCount).isEqualTo(0)
        assertThat(r1.deferred).isFalse()
        assertThat(r1.delayMs).isEqualTo(0)
        assertThat(r1.content).isInstanceOf(TestInformation::class.java)
    }

    class TestInformation(origin: Origin, priority: Priority = B2) : BasicInformation(origin, priority)

    class TestAgent : BasicAgent() {

        override fun accepts(notification: Notification<*>) = when (notification.content) {
            is Request, is TestInformation -> true
            else -> super.accepts(notification)
        }

        override fun receive(notification: Notification<*>) = when(val message = notification.content) {
            is Request -> message.succeed()
            is TestInformation -> relax()
            else -> super.receive(notification)
        }
    }

    companion object {
        init {
            CoraModules.register(
                CoraConfImpl::class,
                CoraComImpl::class,
                CoraTextImpl::class,
                CoraTypeImpl::class,
                CoraControlImpl::class
            )
        }

        val IMMEX = CoraControl.IMMEX
    }
}
