/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.trouble.MemberNotFoundException
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class BusHubImplTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(CoraConfImpl(), CoraTextImpl(), CoraTypeImpl(), CoraComImpl(), CoraControlImpl(), CoraDirImpl())
        }
    }

    @Test
    fun attachDetachReattachSimpleBusHubWith2SimpleNodesFromBeginning() {
        // given:
        val testee = BusHubImpl()
        val node1 = BusNodeImpl()
        val node2 = BusNodeImpl()
        // when:
        testee.add("e1", node1)
        testee.add("e2", node2)
        testee.attach(TestBusContext(TestBusHubView(), "container")).standby()
        // then:
        assertThat(testee.state).isEqualTo(BusNodeState.READY)
        assertThat(node1.state).isEqualTo(BusNodeState.READY)
        assertThat(node2.state).isEqualTo(BusNodeState.READY)
        // when:
        testee.detach().standby()
        // then:
        assertThat(testee.state).isEqualTo(BusNodeState.DETACHED)
        assertThat(node1.state).isEqualTo(BusNodeState.DETACHED)
        assertThat(node2.state).isEqualTo(BusNodeState.DETACHED)
        // when:
        testee.add("egain1", node1)
        testee.add("egain2", node2)
        testee.attach(TestBusContext(TestBusHubView(), "recontainer")).standby()
        // then:
        assertThat(testee.state).isEqualTo(BusNodeState.READY)
        assertThat(node1.state).isEqualTo(BusNodeState.READY)
        assertThat(node2.state).isEqualTo(BusNodeState.READY)
        assertThat(testee.name).isEqualTo("recontainer")
    }

    @Test
    fun attachDetachReattachSimpleBusHubWith2SimpleNodesAfterSetup() {
        // given:
        val testee = BusHubImpl()
        val node1 = BusNodeImpl()
        val node2 = BusNodeImpl()
        // when:
        testee.attach(TestBusContext(TestBusHubView(), "container")).standby()
        testee.add("e1", node1).standby()
        testee.add("e2", node2).standby()
        // then:
        assertThat(testee.state).isEqualTo(BusNodeState.READY)
        assertThat(node1.state).isEqualTo(BusNodeState.READY)
        assertThat(node2.state).isEqualTo(BusNodeState.READY)
        // when:
        testee.detach().standby()
        // then:
        assertThat(testee.state).isEqualTo(BusNodeState.DETACHED)
        assertThat(node1.state).isEqualTo(BusNodeState.DETACHED)
        assertThat(node2.state).isEqualTo(BusNodeState.DETACHED)
        // when:
        testee.attach(TestBusContext(TestBusHubView(), "recontainer")).standby()
        testee.add("egain1", node1).standby()
        testee.add("egain2", node2).standby()
        // then:
        assertThat(testee.state).isEqualTo(BusNodeState.READY)
        assertThat(node1.state).isEqualTo(BusNodeState.READY)
        assertThat(node2.state).isEqualTo(BusNodeState.READY)
        assertThat(testee.name).isEqualTo("recontainer")
    }

    @Test
    fun testMemberFunctions() {
        // given:
        val testee = BusHubImpl()
        val node1 = BusNodeImpl()
        val node2 = BusNodeImpl()
        // when:
        testee.add("e1", node1)
        testee.add("e2", node2)
        testee.attach(TestBusContext(TestBusHubView(), "container")).standby()
        // then:
        assertThat(testee.names.value).containsExactlyInAnyOrder("e1", "e2")
        assertThat(testee.members.value).containsAllEntriesOf(mapOf("e1" to node1, "e2" to node2))
        assertThat(testee.lookup("e1").value).isEqualTo(node1)
        try {
            testee.lookup("e0").value
            fail("Expected failure!")
        } catch (e: RequestFailedException) {
            assertThat(e.cause is MemberNotFoundException)
        }
        // when:
        testee.remove("e1").standby()
        try {
            testee.remove("e0").standby()
            fail("Expected failure!")
        } catch (e: RequestFailedException) {
            assertThat(e.cause is MemberNotFoundException)
        }
        // then:
        assertThat(testee.names.value).containsExactlyInAnyOrder("e2")
        assertThat(testee.members.value).containsAllEntriesOf(mapOf("e2" to node2))
        assertThat(testee.lookup("e2").value).isEqualTo(node2)
        try {
            testee.lookup("e1").value
            fail("Expected failure!")
        } catch (e: RequestFailedException) {
            assertThat(e.cause is MemberNotFoundException)
        }
        // when:
        testee.rename("e2", "e1").standby()
        // then:
        assertThat(testee.names.value).containsExactlyInAnyOrder("e1")
        assertThat(testee.members.value).containsAllEntriesOf(mapOf("e1" to node2))
        assertThat(testee.lookup("e1").value).isEqualTo(node2)
        try {
            testee.lookup("e2").value
            fail("Expected failure!")
        } catch (e: RequestFailedException) {
            assertThat(e.cause is MemberNotFoundException)
        }
        // when:
        testee.replace("e1", node1).standby()
        // then:
        assertThat(testee.names.value).containsExactlyInAnyOrder("e1")
        assertThat(testee.members.value).containsAllEntriesOf(mapOf("e1" to node1))
        assertThat(testee.lookup("e1").value).isEqualTo(node1)
        // finally:
        testee.detach().standby()
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
}
