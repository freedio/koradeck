/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.Notification.Companion.LOST_ITEMS
import com.coradec.coradeck.com.model.NotificationState.LOST
import com.coradec.coradeck.com.model.NotificationState.PROCESSED
import com.coradec.coradeck.com.model.impl.BasicInformation
import com.coradec.coradeck.com.model.impl.BasicNotification
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
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
            CoraModules.register(
                CoraConfImpl::class,
                CoraTypeImpl::class,
                CoraComImpl::class,
                CoraTextImpl::class,
                CoraControlImpl::class
            )
        }
    }

    @Test
    fun testNotificationInjectionCIMMEX() {
        // given
        val agent = TestAgent1()
        val message = TestNotification1(here)
        CIMMEX.subscribe(agent)
        // when
        CIMMEX.inject(message)
        CIMMEX.synchronize()
        agent.synchronize()
        // then
        Thread.sleep(100)
        assertThat(agent.gotMessage).isTrue()
        assertThat(LOST_ITEMS).doesNotContain(message)
        // cleanup
        CIMMEX.unsubscribe(agent)
    }

    @Test
    fun testInformationInjectionCIMMEX() {
        // given
        val agent = TestAgent1()
        val message = TestInformation1(here)
        CIMMEX.subscribe(agent)
        // when
        CIMMEX.inject(message)
        CIMMEX.synchronize()
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isTrue()
        assertThat(LOST_ITEMS.map { it.content }).doesNotContain(message)
        // cleanup
        CIMMEX.unsubscribe(agent)
        CIMMEX.synchronize()
    }

    @Test
    fun testLostNotification() {
        // given
        val message = TestNotification3(here)
        // when
        CIMMEX.inject(message)
        CIMMEX.synchronize()
        // then
        assertThat(message.state).isEqualTo(LOST)
        assertThat(LOST_ITEMS).contains(message)
    }


    @Test
    fun testRecoveredNotification() {
        // given
        val message = TestNotification1(here)
        val agent = TestAgent1()
        // when
        CIMMEX.inject(message)
        CIMMEX.synchronize()
        CIMMEX.subscribe(agent)
        Thread.sleep(100)
        // then
        assertThat(message.state).isEqualTo(PROCESSED)
        // cleanup
        CIMMEX.unsubscribe(agent)
    }

    @Test
    fun testInjectionThroughAgent() {
        // given
        val agent = TestAgent1()
        val info = TestInformation1(here)
        // when
        val message = agent.accept(info)
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isTrue()
        assertThat(message.recipient).isEqualTo(agent)
        assertThat(message.enqueued).isTrue()
        assertThat(message.dispatched).isTrue()
        assertThat(message.delivered).isTrue()
        assertThat(message.processed).isTrue()
    }

    @Test
    fun testDelayedNotificationInjectionCIMMEX() {
        // given
        val then = System.currentTimeMillis()
        val delay = Duration.ofSeconds(2)
        val agent = TestAgent2()
        CIMMEX.subscribe(agent)
        val message = TestNotification2(here, delay)
        // when
        CIMMEX.inject(message)
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isFalse()
        message.standBy()
        assertThat(agent.gotMessage).isTrue()
        assertThat(System.currentTimeMillis()).isGreaterThanOrEqualTo(then + delay.toMillis())
        // cleanup
        CIMMEX.unsubscribe(agent)
    }

    @Test
    fun testDelayedInformationInjectionCIMMEX() {
        // given
        val then = System.currentTimeMillis()
        val delay = Duration.ofSeconds(2)
        val agent = TestAgent2()
        CIMMEX.subscribe(agent)
        val message = TestInformation2(here, delay)
        // when
        CIMMEX.inject(message)
        agent.synchronize()
        // then
        assertThat(agent.gotMessage).isFalse()
        message.standBy()
        assertThat(agent.gotMessage).isTrue()
        assertThat(System.currentTimeMillis()).isGreaterThan(then + delay.toMillis())
        // cleanup
        CIMMEX.unsubscribe(agent)
    }

    class TestInformation1(origin: Origin) : BasicInformation(origin)
    class TestNotification1(origin: Origin) : BasicNotification<TestInformation1>(
        TestInformation1(origin), validUpTo = ZonedDateTime.now() + Duration.ofSeconds(2)
    )
    class TestInformation2(
        origin: Origin,
        delay: Duration
    ) : BasicInformation(origin, validFrom = ZonedDateTime.now().plus(delay)) {
        private val semaphore = Semaphore(0)
        fun standBy() {
            semaphore.acquire()
        }

        fun done() {
            semaphore.release()
        }
    }

    class TestNotification2(
        origin: Origin,
        delay: Duration
    ) : BasicNotification<TestInformation1>(TestInformation1(origin), validFrom = ZonedDateTime.now().plus(delay)) {
        private val semaphore = Semaphore(0)
        fun standBy() {
            semaphore.acquire()
        }

        fun done() {
            semaphore.release()
        }
    }

    class TestInformation3(origin: Origin) : BasicInformation(origin)
    class TestNotification3(origin: Origin) : BasicNotification<TestInformation3>(
        TestInformation3(origin), validUpTo = ZonedDateTime.now() + Duration.ofMillis(1)
    )

    class TestAgent1 : BasicAgent() {
        var gotMessage: Boolean = false

        override fun accepts(notification: Notification<*>) =
            notification.content is TestInformation1 || super.accepts(notification)

        override fun accepts(information: Information): Boolean = information is TestInformation1 || super.accepts(information)

        override fun receive(notification: Notification<*>) = when (notification.content) {
            is TestInformation1 -> gotMessage = true
            else -> super.receive(notification)
        }
    }

    class TestAgent2 : BasicAgent() {
        var gotMessage: Boolean = false

        override fun accepts(notification: Notification<*>) =
            notification is TestNotification2 || notification.content is TestInformation2 || super.accepts(notification)

        override fun accepts(information: Information): Boolean = information is TestInformation2 || super.accepts(information)

        override fun receive(notification: Notification<*>) = when {
            notification is TestNotification2 -> {
                debug("TestNotification2 received.")
                gotMessage = true
                notification.done()
            }
            notification.content is TestInformation2 -> {
                debug("TestNotification2 received.")
                gotMessage = true
                (notification.content as TestInformation2).done()
            }
            else -> super.receive(notification)
        }
    }

}
