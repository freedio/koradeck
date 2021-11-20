/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.session.model.Session
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class BasicBusContext(val session: Session, override var name: String, override val hub: BusHubView) : BusContext {
    private var candidate: MemberView? = null
    override var member: MemberView? = null

    override fun get(type: Class<*>): MemberView? = hub[type]
    override fun get(type: KClass<*>): MemberView? = hub[type]
    override fun leaving() = hub.onLeaving(member ?: throw IllegalStateException("There is no member that could leave!"))
    override fun left() {
        hub.unlink(name)
        hub.onLeft(member ?: throw IllegalStateException("There is no member that could have left!"))
        member = null
    }
    override fun joining(node: MemberView) {
        if (member != null || candidate != null) throw IllegalStateException("The context is occupied!")
        hub.onJoining(node)
        candidate = node
    }
    override fun joined(node: MemberView) {
        if (node !== candidate) throw IllegalStateException("Candidate $node never announced itself!")
        member = node
        candidate = null
        hub.onJoined(node)
    }
    override fun ready() = member?.let {
        hub.onReady(it)
        hub.link(name, it)
    } ?: throw IllegalStateException("There is no member that could become ready!")
    override fun busy() = hub.onBusy(member ?: throw IllegalStateException("There is no member that could become busy!"))
    override fun renameTo(name: String) {
        hub.rename(this.name, name)
        this.name = name
    }

    override fun crashed() {
        member?.apply { hub.onCrashed(this) }
    }
}
