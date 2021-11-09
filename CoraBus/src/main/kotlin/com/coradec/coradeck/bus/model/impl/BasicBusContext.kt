/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class BasicBusContext(override var name: String, override val hub: BusHubView) : BusContext {
    private var candidate: BusNode? = null
    override var member: BusNode? = null

    override fun <D : BusNode> get(type: Class<D>): D? = hub[type]
    override fun <D : BusNode> get(type: KClass<D>): D? = hub[type]
    override fun leaving() = hub.onLeaving(member ?: throw IllegalStateException("There is no member that could leave!"))
    override fun left() {
        hub.unlink(name)
        hub.onLeft(member ?: throw IllegalStateException("There is no member that could have left!"))
        member = null
    }
    override fun joining(node: BusNode) {
        if (member != null || candidate != null) throw IllegalStateException("The context is occupied!")
        hub.onJoining(node)
        candidate = node
    }
    override fun joined(node: BusNode) {
        if (node !== candidate) throw IllegalStateException("Candidate $node never announced itself!")
        member = node
        candidate = null
        hub.link(name, node)
        hub.onJoined(node)
    }
    override fun ready() = member?.let {
        hub.onReady(it)
        hub.link(name, it)
    } ?: throw IllegalStateException("There is no member that could become ready!")
    override fun busy() = hub.onBusy(member ?: throw IllegalStateException("There is no member that could become busy!"))
    override fun rename(name: String) {
        this.name = name
    }
}
