/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusHub
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.module.CoraBus.applicationBus
import com.coradec.coradeck.bus.module.CoraBusImpl
import com.coradec.coradeck.com.module.CoraCom
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

internal class BasicBusNodeTest {

    var myContainer: BusHub? = null
    val container: BusHub get() = myContainer ?: throw IllegalStateException("Container not ready!")

    @BeforeEach fun setupTest() {
        myContainer = TestBusHub()
        applicationBus.add("Container", container.memberView)
    }

    @AfterEach fun tearDownTest() {
        myContainer = null
        if (applicationBus.contains("Container").value) applicationBus.remove("Container").standby()
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

    @Test fun testAttachDetach() {
        // given
        val testee = TestBusNode()
        assertThat(testee.state).isEqualTo(UNATTACHED)
        // when
        container.add("Testee", testee.memberView).standby()
        CoraCom.log.detail("----------- standby returned -----------")
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

    @Test fun testAttachTerminate() {
        // given
        val testee = TestBusNode()
        assertThat(testee.state).isEqualTo(UNATTACHED)
        // when
        container.add("Testee", testee.memberView).standby()
        // then
        assertThat(testee.attached)
        assertThat(testee.state).isEqualTo(READY)
        assertThat(testee.states).contains(INITIALIZED)
        // and when
        container.detach().standby()
        // then
        assertThat(!testee.attached)
        assertThat(testee.state).isEqualTo(DETACHED)
    }

    class TestBusNode: BasicBusNode()
    class TestBusHub : BasicBusHub()
}
