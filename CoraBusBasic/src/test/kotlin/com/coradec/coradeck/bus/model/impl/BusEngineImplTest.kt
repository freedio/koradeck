/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.session.view.View
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class BusEngineImplTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(
                CoraConfImpl::class,
                CoraTextImpl::class,
                CoraTypeImpl::class,
                CoraComImpl::class,
                CoraControlImpl::class,
                CoraDirImpl::class
            )
        }
    }

    @Test
    fun attachDetachReattachSimpleBusEngine() {
        // given:
        val testee = BusEngineImpl()
        // when:
        testee.attach(context = TestBusContext(TestBusHubView(), "einzel")).standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(testee.name).isEqualTo("einzel")
        // when:
        testee.detach().standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.DETACHED)
        // when:
        testee.attach(context = TestBusContext(TestBusHubView(), "wieder")).standby()
        // then:
        Assertions.assertThat(testee.state).isEqualTo(BusNodeState.READY)
        Assertions.assertThat(testee.name).isEqualTo("wieder")
    }

    class TestBusHubView : BusHubView {
        private val states = mutableListOf<String>()

        override fun pathOf(name: String): Path = "=$name"
        override fun get(type: Class<*>): MemberView? = null
        override fun get(type: KClass<*>): MemberView? = null
        override fun onLeaving(member: MemberView) {
            states += "leaving"
        }

        override fun onLeft(member: MemberView) {
            states += "left"
        }

        override fun onJoining(node: MemberView) {
            states += "joining"
        }

        override fun onJoined(node: MemberView) {
            states += "joined"
        }

        override fun onReady(member: MemberView) {
            states += "ready"
        }

        override fun onBusy(member: MemberView) {
            states += "busy"
        }

        override fun onCrashed(member: MemberView) {
            states += "crashed"
        }

        override fun link(name: String, node: MemberView) = relax()
        override fun unlink(name: String) = relax()
        override fun rename(name: String, newName: String) = relax()
    }

    class TestBusContext(
        override val hub: BusHubView,
        override var name: String
    ) : BusContext {
        private val states = mutableListOf<String>()
        override fun get(type: Class<*>): MemberView? = null
        override fun get(type: KClass<*>): MemberView? = null
        override fun <V : View> get(type: Class<*>, viewType: KClass<V>): V? = null
        override fun <V : View> get(type: KClass<*>, viewType: KClass<V>): V? = null

        override val member: MemberView? = null
        override val path: Path = "/test/heinzel"

        override fun leaving() {
            states += "$member leaving"
        }

        override fun left() {
            states += "$member left"
        }

        override fun joining(node: MemberView) {
            states += "$node joining"
        }

        override fun joined(node: MemberView) {
            states += "$node joined"
        }

        override fun ready() {
            states += "$member ready"
        }

        override fun busy() {
            states += "$member busy"
        }

        override fun crashed() {
            states += "$member crashed"
        }

        override fun renameTo(name: String) {
            this.name = name
        }
    }

}
