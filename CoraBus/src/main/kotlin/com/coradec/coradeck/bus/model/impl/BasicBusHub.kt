/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusHubDelegate
import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.model.DelegatedBusHub
import com.coradec.coradeck.bus.model.HubDelegator
import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.module.CoraDir

open class BasicBusHub(protected val namespace: DirectoryNamespace = CoraDir.defaultNamespace) : BasicBusNode(), DelegatedBusHub {
    override val delegate: BusHubDelegate = CoraBus.createHub(InternalHubDelegator())
    override val members: Voucher<Map<String, BusNode>> get() = delegate.members
    override val names: Voucher<Set<String>> get() = delegate.names

    override fun lookup(name: String): Voucher<BusNode> = delegate.lookup(name)
    override fun add(name: String, node: BusNode): Request = delegate.add(name, node)
    override fun remove(name: String): Voucher<BusNode> = delegate.remove(name)
    override fun replace(name: String, substitute: BusNode): Voucher<BusNode> = delegate.replace(name, substitute)
    override fun rename(name: String, newName: String): Request = delegate.rename(name, newName)

    protected fun leave() = delegate.leave()

    protected open fun onLoading() {}
    protected open fun onLoaded() {}
    protected open fun onUnloading() {}
    protected open fun onUnloaded() {}
    protected open fun onJoining(name: String, node: BusNode) {}
    protected open fun onJoined(name: String, node: BusNode) {}
    protected open fun onLeaving(name: String, node: BusNode) {}
    protected open fun onLeft(name: String, node: BusNode) {}

    protected open inner class InternalHubDelegator : InternalNodeDelegator(), HubDelegator {
        override val node: BusNode = this@BasicBusHub
        override fun onJoining(name: String, node: BusNode) = this@BasicBusHub.onJoining(name, node)
        override fun onJoined(name: String, node: BusNode) = this@BasicBusHub.onJoined(name, node)
        override fun onLeaving(name: String, node: BusNode) = this@BasicBusHub.onLeaving(name, node)
        override fun onLeft(name: String, node: BusNode) = this@BasicBusHub.onLeft(name, node)
        override fun onLoading() = this@BasicBusHub.onLoading()
        override fun onLoaded() = this@BasicBusHub.onLoaded()
        override fun onUnloading() = this@BasicBusHub.onUnloading()
        override fun onUnloaded() = this@BasicBusHub.onUnloaded()
        override fun toString() = this@BasicBusHub.toString()
    }
}
