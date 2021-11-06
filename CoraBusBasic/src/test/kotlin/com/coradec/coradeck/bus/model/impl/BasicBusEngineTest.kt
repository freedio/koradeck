/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusHub
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.bus.module.CoraBusImpl
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

internal class BasicBusEngineTest {

    var myContainer: BusHub? = null
    private val container: BusHub get() = myContainer ?: throw IllegalStateException("Container not ready!")

    @BeforeEach
    fun setupTest() {
        myContainer = TestBusHub()
        CoraBus.applicationBus.add("Container", container).standby()
    }

    @AfterEach
    fun tearDownTest() {
        myContainer = null
        CoraBus.applicationBus.remove("Container").standby()
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(
                CoraConfImpl(),
                CoraTextImpl(),
                CoraTypeImpl(),
                CoraComImpl(),
                CoraControlImpl(),
                CoraDirImpl(),
                CoraBusImpl()
            )
        }
    }

    @Test
    fun testAttachDetach() {
        // given
        val testee = TestBusEngine()
        assertThat(testee.state).isEqualTo(UNATTACHED)
        assertThat(testee.running.get()).isFalse()
        // when
        container.add("Testee", testee)
        testee.standby()
        // then
        assertThat(testee.attached)
        assertThat(testee.state).isEqualTo(READY)
        assertThat(testee.states).contains(INITIALIZED)
        // and when
        container.remove("Testee").standby()
        // then
        assertThat(!testee.attached)
        assertThat(testee.state).isEqualTo(DETACHED)
    }

    class TestBusEngine : BasicBusEngine() {
        var running = AtomicBoolean(false)
        override fun run() {
            running.set(true)
        }
    }

    class TestBusHub : BasicBusHub()
}
