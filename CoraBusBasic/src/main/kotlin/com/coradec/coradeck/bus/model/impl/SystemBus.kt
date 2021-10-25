/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.core.util.addShutdownHook
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDir
import kotlin.reflect.KClass

object SystemBus : BasicBusHub(CoraDir.rootNamespace) {
    private const val SYSTEM_BUS_NAME = "system"
    private val selfContext = SystemBusContext()

    init {
        attach(selfContext)
        addShutdownHook("Blackout") { if (attached) detach().standby() }
    }

    @Suppress("UNCHECKED_CAST")
    class SystemBusContext : BasicBusContext(SYSTEM_BUS_NAME, DummyHub()) {
        override val hub: BusHubView get() = throw UnsupportedOperationException()
        override var member: BusNode? = null

        override fun <D : BusNode> get(type: Class<D>): D? =
            if (type.isInstance(SystemBus)) SystemBus as D else null

        override fun <D : BusNode> get(type: KClass<D>): D? =
            if (type.isInstance(SystemBus)) SystemBus as D else null

        override fun joining(node: BusNode) {
            if (node !is SystemBus) throw IllegalArgumentException("Only accepted applicant is the system bus.")
            debug("System Bus is joining the system bus context.")
        }

        override fun joined(node: BusNode) {
            if (node !is SystemBus) throw IllegalArgumentException("Only accepted applicant is the system bus.")
            member = node
            debug("System Bus joined the system bus context.")
        }

        override fun ready() {
            debug("System Bus is ready.")
        }

        override fun busy() {
            debug("System Bus becomes busy.")
        }

        override fun leaving() {
            debug("System bus is leaving the system bus context.")
        }

        override fun left() {
            member = null
            debug("System bus left the system bus context.")
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
    }
}
