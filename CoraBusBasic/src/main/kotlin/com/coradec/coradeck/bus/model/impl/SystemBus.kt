/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.delegation.BusHubDelegate
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.trouble.InvalidRequestException
import com.coradec.coradeck.core.util.addShutdownHook
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDir
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.trouble.ViewNotFoundException
import com.coradec.coradeck.session.view.View
import com.coradec.coradeck.session.view.impl.BasicView
import com.coradec.coradeck.text.model.LocalText
import kotlin.reflect.KClass

object SystemBus : BasicBusHub(CoraDir.rootNamespace) {
    private const val SYSTEM_BUS_NAME = "system"
    private val selfContext = SystemBusContext(Session.new)
    private val CIMMEX = CoraControl.IMMEX
    private val TEXT_ATTACHING = LocalText("Attaching")
    private val TEXT_ATTACHED = LocalText("Attached")
    private val TEXT_READY = LocalText("Ready")
    private val TEXT_BUSY = LocalText("Busy")
    private val TEXT_DETACHING = LocalText("Detaching")
    private val TEXT_SHUTDOWN = LocalText("Shutdown")

    init {
        attach(context = selfContext) andThen { CIMMEX.preventShutdown() }
        addShutdownHook("Blackout") {
            if (attached) detach() andThen { CIMMEX.allowShutdown() } else CIMMEX.allowShutdown()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class SystemBusContext(session: Session) : BasicBusContext(session, SYSTEM_BUS_NAME, DummyHub(session)) {
        override val hub: BusHubView get() = throw UnsupportedOperationException()
        override val path: Path get() = namespace.concat("", name)
        override var member: MemberView? = null

        override fun get(type: Class<*>): MemberView? =
            if (type.isInstance(SystemBus)) session.view[SystemBus, SystemBusView::class] else null

        override fun get(type: KClass<*>): MemberView? =
            if (type.isInstance(SystemBus)) session.view[SystemBus, SystemBusView::class] else null

        override fun joining(node: MemberView) {
            if (node is BusHubDelegate && node.delegator?.node !is SystemBusView)
                throw IllegalArgumentException("Only accepted applicant is the system bus.")
            info(TEXT_ATTACHING)
        }

        override fun joined(node: MemberView) = true.also {
            if (node is BusHubDelegate && node.delegator?.node !is SystemBusView)
                throw IllegalArgumentException("Only accepted applicant is the system bus.")
            member = node
            info(TEXT_ATTACHED)
        }

        override fun ready() {
            info(TEXT_READY)
        }

        override fun busy() {
            info(TEXT_BUSY)
        }

        override fun leaving() {
            info(TEXT_DETACHING)
        }

        override fun left(): Boolean = true.also {
            member = null
            info(TEXT_SHUTDOWN)
        }
    }

    private class SystemBusView(session: Session) : BasicView(session), MemberView {
        override fun attach(context: BusContext): Request = throw InvalidRequestException("SystemBus cannot be attached!")
        override fun detach(): Request = throw InvalidRequestException("SystemBus cannot be detached!")
        override fun <V : View> lookupView(session: Session, type: KClass<V>): V? = null
        override fun <V : View> getView(session: Session, type: KClass<V>): V = lookupView(session, type)
            ?: throw ViewNotFoundException(SystemBus::class, type)
    }

    private class DummyHub(override val session: Session) : BusHubView {
        override fun pathOf(name: String): Path = name
        override fun get(type: Class<*>): MemberView? = null
        override fun get(type: KClass<*>): MemberView? = null
        override fun onLeaving(name: String, member: MemberView) = relax()
        override fun onLeft(name: String, member: MemberView): Boolean = true
        override fun onJoining(name: String, node: MemberView) = relax()
        override fun onJoined(name: String, node: MemberView): Boolean = true
        override fun onReady(member: MemberView) = relax()
        override fun onBusy(member: MemberView) = relax()
        override fun onCrashed(member: MemberView) = relax()
        override fun link(name: String, node: MemberView) = relax()
        override fun unlink(name: String) = relax()
        override fun rename(name: String, newName: String) = relax()
    }
}
