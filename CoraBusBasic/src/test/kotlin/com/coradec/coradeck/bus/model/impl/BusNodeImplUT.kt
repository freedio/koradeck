/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNodeState.DETACHED
import com.coradec.coradeck.bus.model.BusNodeState.READY
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
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.view.View
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class BusNodeImplUT {

    @Test
    fun attachDetachReattachSimpleBusNode() {
        // given:
        val testee = BusNodeImpl()
        // when:
        testee.attach(context = TestBusContext(TestBusHubView(Session.current), "einzel")).standby()
        testee.standby()
        // then:
        assertThat(testee.state).isEqualTo(READY)
        assertThat(testee.name).isEqualTo("einzel")
        // when:
        testee.detach().standby()
        Thread.sleep(20)
        // then:
        assertThat(testee.state).isEqualTo(DETACHED)
        // when:
        testee.attach(context = TestBusContext(TestBusHubView(Session.current), "wieder")).standby()
        testee.standby()
        // then:
        assertThat(testee.state).isEqualTo(READY)
        assertThat(testee.name).isEqualTo("wieder")
    }

    class TestBusHubView(override val session: Session) : BusHubView {
        private val states = mutableListOf<String>()

        override fun pathOf(name: String): Path = "=$name"
        override fun get(type: Class<*>): MemberView? = null
        override fun get(type: KClass<*>): MemberView? = null
        override fun onLeaving(name: String, member: MemberView) {
            states += "leaving"
        }

        override fun onLeft(name: String, member: MemberView) = true.also {
            states += "left"
        }

        override fun onJoining(name: String, node: MemberView) {
            states += "joining"
        }

        override fun onJoined(name: String, node: MemberView) = true.also {
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

        override fun left() = true.also {
            states += "$member left"
        }

        override fun joining(node: MemberView) {
            states += "$node joining"
        }

        override fun joined(node: MemberView) = true.also {
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

}
