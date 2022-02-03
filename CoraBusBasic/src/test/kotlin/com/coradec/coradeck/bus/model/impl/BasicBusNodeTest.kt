/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.module.CoraBus.applicationBus
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
import org.junit.jupiter.api.Test

internal class BasicBusNodeTest {

    init {
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

    @Test fun testAttachDetach() {
        // given
        val container = BasicBusHub().apply { applicationBus.add("Container", memberView) }
        val testee = BasicBusNode()
        assertThat(testee.state).isEqualTo(UNATTACHED)
        // when
        container.add("Testee", testee.memberView).standby()
        // then
        assertThat(testee.attached)
        assertThat(testee.state).isEqualTo(READY)
        assertThat(testee.states).contains(INITIALIZED)
        Thread.sleep(1000)
        // and when
        container.detach().standby()
        // then
        assertThat(!testee.attached)
        assertThat(testee.state).isEqualTo(DETACHED)
        // cleanup
        try {
            applicationBus.remove("Container").standby()
        } catch (e: MemberNotFoundException) {
            // that's OK.
        } catch (e: RequestFailedException) {
            // that's OK.
        }
    }

    @Test fun testAttachTerminate() {
        // given
        val container = BasicBusHub().apply { applicationBus.add("Container", memberView) }
        val testee = BasicBusNode()
        assertThat(testee.state).isEqualTo(UNATTACHED)
        // when
        container.add("Testee", testee.memberView).standby()
        // then
        assertThat(testee.attached)
        assertThat(testee.state).isEqualTo(READY)
        assertThat(testee.states).contains(INITIALIZED)
        Thread.sleep(1000)
        // and when
        container.remove("Testee").standby()
        // then
        assertThat(!testee.attached)
        assertThat(testee.state).isEqualTo(DETACHED)
        // cleanup
        try {
            applicationBus.remove("Container").standby()
        } catch (e: MemberNotFoundException) {
            // that's OK.
        } catch (e: RequestFailedException) {
            // that's OK.
        }
    }
}
