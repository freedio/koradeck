/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.bus.module.CoraBusImpl
import com.coradec.coradeck.bus.trouble.MemberNotFoundException
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

internal class BasicBusEngineTest {
    @Test
    fun testAttachDetach() {
        // given
        println("************ running ************")
        val container = BasicBusHub().apply { CoraBus.applicationBus.add("Container", memberView) }
        val testee = TestBusEngine()
        assertThat(testee.state).isEqualTo(UNATTACHED)
        assertThat(testee.running.get()).isFalse()
        // when
        container.add("Testee", testee.memberView).standby()
        // then
        assertThat(testee.attached)
        assertThat(testee.state).isEqualTo(READY)
        assertThat(testee.states).contains(INITIALIZED)
        // and when
        container.remove("Testee").standby()
        Thread.sleep(100)
        // then
        assertThat(!testee.attached)
        assertThat(testee.state).isEqualTo(DETACHED)
        // cleanup
        try {
            CoraBus.applicationBus.remove("Container").standby()
        } catch (e: MemberNotFoundException) {
            // that's OK.
        } catch (e: RequestFailedException) {
            // that's OK.
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            println("************ start registering ************")
            CoraModules.register(
                CoraConfImpl::class,
                CoraTextImpl::class,
                CoraTypeImpl::class,
                CoraComImpl::class,
                CoraControlImpl::class,
                CoraDirImpl::class,
                CoraBusImpl::class
            )
            println("************ finished registering ************")
        }
    }

    class TestBusEngine : BasicBusEngine() {
        var running = AtomicBoolean(false)
        override fun run() {
            running.set(true)
            while (!Thread.interrupted()) {
                debug("Waiting for interruption.")
                Thread.sleep(1000)
            }
        }
    }
}
