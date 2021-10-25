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

class BusMachineImplTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl(), CoraDirImpl())
        }
    }

    @Test
    fun attachDetachReattachSimpleBusMachineWith2SimpleNodesFromBeginning() {
        // given:
        val testee = BusMachineImpl()
        val node1 = BusNodeImpl()
        val node2 = BusEngineImpl()
        // when:
        testee.add("node", node1)
        testee.add("engine", node2)
        testee.attach(TestBusContext(TestBusHubView(), "machine")).standby()
        Thread.sleep(100)
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node1.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node2.state).isEqualTo(BusNodeState.READY)
        // when:
        testee.detach().standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.DETACHED)
        Assertions.assertThat(node1.state).isEqualTo(BusNodeState.DETACHED)
        Assertions.assertThat(node2.state).isEqualTo(BusNodeState.DETACHED)
        // when:
        testee.add("nodegain1", node1)
        testee.add("enginegain2", node2)
        testee.attach(TestBusContext(TestBusHubView(), "remachine")).standby()
        Thread.sleep(100)
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node1.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node2.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(testee.name).isEqualTo("remachine")
    }

    @Test
    fun attachDetachReattachSimpleBusMachineWith2SimpleNodesAfterSetup() {
        // given:
        val testee = BusMachineImpl()
        val node1 = BusNodeImpl()
        val node2 = BusEngineImpl()
        // when:
        testee.attach(TestBusContext(TestBusHubView(), "machine")).standby()
        testee.add("node", node1).standby()
        testee.add("engine", node2).standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node1.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node2.state).isEqualTo(BusNodeState.READY)
        // when:
        testee.detach().standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.DETACHED)
        Assertions.assertThat(node1.state).isEqualTo(BusNodeState.DETACHED)
        Assertions.assertThat(node2.state).isEqualTo(BusNodeState.DETACHED)
        // when:
        testee.attach(TestBusContext(TestBusHubView(), "remachine")).standby()
        testee.add("nodegain1", node1).standby()
        testee.add("enginegain2", node2).standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node1.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(node2.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(testee.name).isEqualTo("remachine")
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
