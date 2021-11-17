/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusHubDelegate
import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.core.util.addShutdownHook
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDir
import com.coradec.coradeck.text.model.LocalText
import kotlin.reflect.KClass

object SystemBus : BasicBusHub(CoraDir.rootNamespace) {
    private const val SYSTEM_BUS_NAME = "system"
    private val selfContext = SystemBusContext()
    private val CIMMEX = CoraControl.IMMEX
    private val TEXT_ATTACHING = LocalText("Attaching")
    private val TEXT_ATTACHED = LocalText("Attached")
    private val TEXT_READY = LocalText("Ready")
    private val TEXT_BUSY = LocalText("Busy")
    private val TEXT_DETACHING = LocalText("Detaching")
    private val TEXT_SHUTDOWN = LocalText("Shutdown")

    init {
        attach(selfContext) andThen { CIMMEX.preventShutdown() }
        addShutdownHook("Blackout") {
            if (attached) detach() andThen { CIMMEX.allowShutdown() }
        }
    }

    @Suppress("UNCHECKED_CAST")
    class SystemBusContext : BasicBusContext(SYSTEM_BUS_NAME, DummyHub()) {
        override val hub: BusHubView get() = throw UnsupportedOperationException()
        override val path: Path get() = namespace.concat("", name)
        override var member: BusNode? = null

        override fun <D : BusNode> get(type: Class<D>): D? =
            if (type.isInstance(SystemBus)) SystemBus as D else null

        override fun <D : BusNode> get(type: KClass<D>): D? =
            if (type.isInstance(SystemBus)) SystemBus as D else null

        override fun joining(node: BusNode) {
            if (node is BusHubDelegate && node.delegator?.node != SystemBus)
                throw IllegalArgumentException("Only accepted applicant is the system bus.")
            info(TEXT_ATTACHING)
        }

        override fun joined(node: BusNode) {
            if (node is BusHubDelegate && node.delegator?.node != SystemBus)
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

        override fun left() {
            member = null
            info(TEXT_SHUTDOWN)
        }
    }

    class DummyHub : BusHubView {
        override fun pathOf(name: String): Path = name
        override fun <D : BusNode> get(type: Class<D>): D? = null
        override fun <D : BusNode> get(type: KClass<D>): D? = null
        override fun onLeaving(member: BusNode) = relax()
        override fun onLeft(member: BusNode) = relax()
        override fun onJoining(node: BusNode) = relax()
        override fun onJoined(node: BusNode) = relax()
        override fun onReady(member: BusNode) = relax()
        override fun onBusy(member: BusNode) = relax()
        override fun onCrashed(member: BusNode) = relax()
        override fun link(name: String, node: BusNode) = relax()
        override fun unlink(name: String) = relax()
    }
}
