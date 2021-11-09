/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.trouble.MemberNotFoundException
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.assertj.core.api.SoftAssertions
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
        node1.standby()
        node2.standby()
        // then:
        var softly = SoftAssertions()
        softly.assertThat(testee.state).isEqualTo(READY)
        softly.assertThat(node1.state).isEqualTo(READY)
        softly.assertThat(node2.state).isEqualTo(READY)
        softly.assertAll()
        // when:
        testee.detach().standby()
        // then:
        softly = SoftAssertions()
        softly.assertThat(testee.state).isEqualTo(DETACHED)
        softly.assertThat(node1.state).isEqualTo(DETACHED)
        softly.assertThat(node2.state).isEqualTo(DETACHED)
        softly.assertAll()
        // when:
        testee.add("egain1", node1)
        testee.add("egain2", node2)
        testee.attach(TestBusContext(TestBusHubView(), "recontainer")).standby()
        node1.standby()
        node2.standby()
        // then:
        CoraCom.log.debug("-------------------------------------------------------------------")
        softly = SoftAssertions()
        softly.assertThat(testee.state).isEqualTo(READY)
        softly.assertThat(node1.state).isIn(INITIALIZED, READY)
        softly.assertThat(node2.state).isIn(INITIALIZED, READY)
        softly.assertThat(testee.name).isEqualTo("recontainer")
        softly.assertAll()
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
        var softly = SoftAssertions()
        softly.assertThat(testee.state).isEqualTo(READY)
        softly.assertThat(node1.state).isEqualTo(READY)
        softly.assertThat(node2.state).isEqualTo(READY)
        softly.assertAll()
        // when:
        testee.detach().standby()
        // then:
        softly = SoftAssertions()
        softly.assertThat(testee.state).isEqualTo(DETACHED)
        softly.assertThat(node1.state).isEqualTo(DETACHED)
        softly.assertThat(node2.state).isEqualTo(DETACHED)
        softly.assertAll()
        // when:
        testee.attach(TestBusContext(TestBusHubView(), "recontainer")).standby()
        testee.add("egain1", node1).standby()
        testee.add("egain2", node2).standby()
        node1.standby()
        node2.standby()
        // then:
        CoraCom.log.debug("-------------------------------------------------------------------")
        softly = SoftAssertions()
        softly.assertThat(testee.state).isEqualTo(READY)
        softly.assertThat(node1.state).isIn(INITIALIZED, READY)
        softly.assertThat(node2.state).isIn(INITIALIZED, READY)
        softly.assertThat(testee.name).isEqualTo("recontainer")
        softly.assertAll()
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
        CoraCom.log.debug("============================================================")
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
        CoraCom.log.debug("***********************************************************")
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

        override fun link(name: String, node: BusNode) = relax()
        override fun unlink(name: String) = relax()
    }

    class TestBusContext(
        override val hub: BusHubView,
        override var name: String
    ) : BusContext {
        private val states = mutableListOf<String>()
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

        override fun rename(name: String) {
            this.name = name
        }

    }
}
