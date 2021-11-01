/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.dir.model.Path
import java.util.concurrent.TimeUnit

open class BasicBusNode : BasicAgent(), DelegatedBusNode {
    override val delegate: BusNodeDelegate = CoraBus.createNode(InternalNodeDelegator())
    override val ready: Boolean get() = delegate.ready
    override val state: BusNodeState get() = delegate.state
    override val states: List<BusNodeState> get() = delegate.states
    override val attached: Boolean get() = /*delegate != null &&*/ delegate.attached // TODO bizarrely, it IS sometimes null
    override val context: BusContext? get() = delegate.context
    override val path: Path? get() = context?.path
    override val name: String? get() = context?.name
    override fun attach(context: BusContext) = delegate.attach(context)
    override fun detach() = delegate.detach()
    override fun context(timeout: Long, timeoutUnit: TimeUnit): BusContext = delegate.context(timeout, timeoutUnit)
    override fun standby(delay: Timespan, state: BusNodeState) = delegate.standby(delay, state)
    override fun standby(state: BusNodeState) = delegate.standby(state)
    override fun standby(delay: Timespan) = delegate.standby(delay)
    override fun standby() = delegate.standby()

    protected open fun onAttaching(context: BusContext) {}
    protected open fun onAttached(context: BusContext) {}
    protected open fun onInitializing() {}
    protected open fun onInitialized() {}
    protected open fun onFinalizing() {}
    protected open fun onFinalized() {}
    protected open fun onDetaching(forced: Boolean) {}
    protected open fun onDetached() {}
    protected open fun onReady() {}
    protected open fun onBusy() {}

    protected open inner class InternalNodeDelegator : NodeDelegator {
        override val node: BusNode = this@BasicBusNode
        override fun onAttaching(context: BusContext) = this@BasicBusNode.onAttaching(context)
        override fun onAttached(context: BusContext) = this@BasicBusNode.onAttached(context)
        override fun onInitializing() = this@BasicBusNode.onInitializing()
        override fun onInitialized() = this@BasicBusNode.onInitialized()
        override fun onFinalizing() = this@BasicBusNode.onFinalizing()
        override fun onFinalized() = this@BasicBusNode.onFinalized()
        override fun onDetaching(forced: Boolean) = this@BasicBusNode.onDetaching(forced)
        override fun onDetached() = this@BasicBusNode.onDetached()
        override fun onReady() = this@BasicBusNode.onReady()
        override fun onBusy() = this@BasicBusNode.onBusy()
    }
}
