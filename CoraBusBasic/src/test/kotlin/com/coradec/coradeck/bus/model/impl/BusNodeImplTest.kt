/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusHub
import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class BusNodeImplTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl(), CoraDirImpl())
        }
    }

    @Test
    fun attachDetachReattachSimpleBusNode() {
        // given:
        val testee = BusNodeImpl()
        // when:
        testee.attach(TestBusContext(TestBusHubView(), "einzel")).standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(testee.name).isEqualTo("einzel")
        // when:
        testee.detach().standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.DETACHED)
        // when:
        testee.attach(TestBusContext(TestBusHubView(), "wieder")).standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(testee.name).isEqualTo("wieder")
    }

    class TestBusHubView : BusHubView {
        private val states = mutableListOf<String>()

        override fun pathOf(name: String): Path = "=$name"
        override fun <D : BusNode> get(type: Class<D>): D? = null
        override fun <D : BusNode> get(type: KClass<D>): D? = null
        override fun onLeaving(member: BusNode) {
            states += "leaving"
        }

        override fun onLeft(member: BusNode) {
            states += "left"
        }

        override fun onJoining(node: BusNode) {
            states += "joining"
        }

        override fun onJoined(node: BusNode) {
            states += "joined"
        }

        override fun onReady(member: BusNode) {
            states += "ready"
        }

        override fun onBusy(member: BusNode) {
            states += "busy"
        }
    }

    class TestBusContext(
        override val hub: BusHubView,
        override val name: String
    ) : BusContext {
        val states = mutableListOf<String>()
        override fun <D : BusNode> get(type: Class<D>): D? = null
        override fun <D : BusNode> get(type: KClass<D>): D? = null
        override val member: BusNode? = null
        override val path: Path = "/test/heinzel"

        override fun leaving() {
            states += "$member leaving"
        }

        override fun left() {
            states += "$member left"
        }

        override fun joining(node: BusNode) {
            states += "$node joining"
        }

        override fun joined(node: BusNode) {
            states += "$node joined"
        }

        override fun ready() {
            states += "$member ready"
        }

        override fun busy() {
            states += "$member busy"
        }
    }

    class TestBusHub : BusHubImpl(), BusHub
}
