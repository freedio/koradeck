/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.delegation.BusHubDelegate
import com.coradec.coradeck.bus.model.delegation.DelegatedBusHub
import com.coradec.coradeck.bus.model.delegation.HubDelegator
import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.module.CoraDir
import com.coradec.coradeck.session.model.Session

open class BasicBusHub(protected val namespace: DirectoryNamespace = CoraDir.defaultNamespace) : BasicBusNode(), DelegatedBusHub {
    override val delegate: BusHubDelegate = CoraBus.createHub(InternalHubDelegator())
    override val members: Voucher<Map<String, MemberView>> get() = delegate.members
    override val names: Voucher<Set<String>> get() = delegate.names

    override fun lookup(name: String): Voucher<MemberView> = delegate.lookup(name)
    override fun add(name: String, node: MemberView): Request = delegate.add(name, node)
    override fun remove(name: String): Voucher<MemberView> = delegate.remove(name)
    override fun replace(name: String, substitute: MemberView): Voucher<MemberView> = delegate.replace(name, substitute)
    override fun rename(name: String, newName: String): Request = delegate.rename(name, newName)
    override fun contains(name: String): Voucher<Boolean> = delegate.contains(name)
    override val memberView: MemberView get() = memberView(Session.current)

    protected open fun onLoading() {}
    protected open fun onLoaded(): Boolean = true
    protected open fun onUnloading() {}
    protected open fun onUnloaded(): Boolean = true
    protected open fun onJoining(name: String, node: MemberView) {}
    protected open fun onJoined(name: String, node: MemberView): Boolean = true
    protected open fun onLeaving(name: String, node: MemberView) {}
    protected open fun onLeft(name: String, node: MemberView): Boolean = true

    protected open inner class InternalHubDelegator : InternalNodeDelegator(), HubDelegator {
        override val node: MemberView get() = this@BasicBusHub.memberView
        override fun onJoining(name: String, node: MemberView) = this@BasicBusHub.onJoining(name, node)
        override fun onJoined(name: String, node: MemberView) = this@BasicBusHub.onJoined(name, node)
        override fun onLeaving(name: String, node: MemberView) = this@BasicBusHub.onLeaving(name, node)
        override fun onLeft(name: String, node: MemberView) = this@BasicBusHub.onLeft(name, node)
        override fun onLoading() = this@BasicBusHub.onLoading()
        override fun onLoaded() = this@BasicBusHub.onLoaded()
        override fun onUnloading() = this@BasicBusHub.onUnloading()
        override fun onUnloaded() = this@BasicBusHub.onUnloaded()
        override fun toString() = this@BasicBusHub.toString()
    }
}
