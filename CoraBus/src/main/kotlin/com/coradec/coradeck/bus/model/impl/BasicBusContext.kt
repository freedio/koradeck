/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.view.View
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class BasicBusContext(val session: Session, override var name: String, override val hub: BusHubView) : BusContext {
    private var candidate: MemberView? = null
    override var member: MemberView? = null

    override fun get(type: Class<*>): MemberView? = hub[type]
    override fun get(type: KClass<*>): MemberView? = hub[type]
    override fun <V : View> get(type: Class<*>, viewType: KClass<V>): V? = get(type)?.getView(session, viewType)
    override fun <V : View> get(type: KClass<*>, viewType: KClass<V>): V? = get(type)?.getView(session, viewType)

    override fun leaving() = hub.onLeaving(name, member ?: throw IllegalStateException("There is no member that could leave!"))
    override fun left(): Boolean {
        hub.unlink(name)
        return hub.onLeft(name, member ?: throw IllegalStateException("There is no member that could have left!"))
            .also { member = null }
    }
    override fun joining(node: MemberView) {
        if (member != null || candidate != null) throw IllegalStateException("The context is occupied!")
        hub.onJoining(name, node)
        candidate = node
    }
    override fun joined(node: MemberView): Boolean {
        if (node !== candidate) throw IllegalStateException("Candidate $node never announced itself!")
        member = node
        candidate = null
        return hub.onJoined(name, node)
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
