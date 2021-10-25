/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.com.*
import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.bus.trouble.MemberNotFoundException
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDir
import java.util.concurrent.Semaphore
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class BusHubImpl(
    private val delegator: HubDelegator? = null,
    private val namespace: DirectoryNamespace = CoraDir.rootNamespace
) : BusNodeImpl(delegator), BusHubDelegate {
    override val members: Voucher<Map<String, BusNode>> get() = MemberVoucher(this).apply { accept(this) }
    override val names: Voucher<Set<String>> get() = NamesVoucher(this).apply { accept(this) }
    private val myMembers = mutableMapOf<String, BusNode>()
    private val candidates = mutableMapOf<String, BusNode>()
    private val memberCheck = Semaphore(0)
    override val mytype = "hub"
    override val myType = "Hub"
    override val upstates: Sequence<BusNodeState> get() = super.upstates + sequenceOf(BusNodeState.LOADING, BusNodeState.LOADED)
    override val downstates: Sequence<BusNodeState>
        get() = sequenceOf(
            BusNodeState.UNLOADING,
            BusNodeState.UNLOADED
        ) + super.downstates

    init {
        route(LookupMemberVoucher::class, ::lookupMember)
        route(AddMemberRequest::class, ::addMember)
        route(RemoveMemberVoucher::class, ::removeMember)
        route(ReplaceMemberVoucher::class, ::replaceMember)
        route(RenameMemberRequest::class, ::renameMember)
        route(AcceptCandidate::class, ::acceptCandidate)
        route(AcceptMember::class, ::acceptMember)
        route(RemoveMember::class, ::removeMember)
        route(MemberVoucher::class, ::members)
        route(NamesVoucher::class, ::names)
    }

    private fun lookupMember(voucher: LookupMemberVoucher) {
        val memberName = voucher.name
        myMembers[memberName]?.apply {
            voucher.value = this
            voucher.succeed()
        } ?: voucher.fail(MemberNotFoundException(memberName))
    }

    private fun members(voucher: MemberVoucher) {
        voucher.value = myMembers
        voucher.succeed()
    }

    private fun names(voucher: NamesVoucher) {
        voucher.value = myMembers.keys
        voucher.succeed()
    }

    private fun removeMember(request: RemoveMember) {
        myMembers -= request.name
    }

    private fun acceptCandidate(request: AcceptCandidate) {
        val name = request.name
        val candidate = candidates[name]
        if (candidate != null) myMembers[name] = candidate
        candidates -= name
        debug("Candidate ‹%s› accepted as member.", name)
        request.succeed()
    }

    private fun acceptMember(request: AcceptMember) {
        myMembers[request.name] = request.node
        request.succeed()
    }

    private fun addMember(request: AddMemberRequest) {
        try {
            val name = request.name
            val node = request.node
            if (attached) accept(node.attach(InternalBusContext(name, InternalHusHubView()))
                    andThen { accept(AcceptMember(name, node).propagateTo(request)) })
            else {
                candidates[name] = node
                request.succeed()
            }
            debug("Candidates: %s, Members: %s.", candidates.entries, myMembers.entries)
        } catch (e: Exception) {
            request.fail(e)
        }
    }

    private fun removeMember(voucher: RemoveMemberVoucher) {
        val name = voucher.name
        myMembers.remove(name)?.apply {
            voucher.value = this
            voucher.succeed()
        } ?: voucher.fail(MemberNotFoundException(name))
    }

    private fun replaceMember(voucher: ReplaceMemberVoucher) {
        val name = voucher.name
        val node = voucher.substitute
        myMembers[name] = node
        voucher.value = this
        voucher.succeed()
    }

    private fun renameMember(request: RenameMemberRequest) {
        val name0 = request.name
        val name1 = request.newName
        myMembers.remove(name0)?.apply {
            myMembers[name1] = this
            request.succeed()
        } ?: request.fail(MemberNotFoundException(name0))
    }

    override fun stateChanged(transition: BusNodeStateTransition) {
        try {
            val context = transition.context
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here!")
            when (transition.unto) {
                BusNodeState.INITIALIZED -> {
                    delegator?.onInitialized()
                    debug("Initialized %s ‹%s›.", mytype, name)
                    state = BusNodeState.INITIALIZED
                }
                BusNodeState.LOADING -> {
                    debug("Loading %s ‹%s›.", mytype, name)
                    delegator?.onLoading()
                    state = BusNodeState.LOADING
                    accept(
                        CoraControl.createItemSet(this, candidates.map { (name, node) ->
                            node.attach(InternalBusContext(name, InternalHusHubView())) andThen { accept(AcceptCandidate(name)) }
                        }, this).propagateTo(transition)
                    )
                    return // avoid succeeding prematurely
                }
                BusNodeState.LOADED -> {
                    delegator?.onLoaded()
                    debug("Loaded %s ‹%s›.", mytype, name)
                    state = BusNodeState.LOADED
                    readify(name)
                }
                BusNodeState.UNLOADING -> {
                    busify(name)
                    debug("Unloading %s ‹%s›.", mytype, name)
                    delegator?.onUnloading()
                    state = BusNodeState.UNLOADING
                    unloadMembers(transition)
                    return // avoid succeeding prematurely
                }
                BusNodeState.UNLOADED -> {
                    delegator?.onLoaded()
                    debug("Unloaded %s ‹%s›.", mytype, name)
                    state = BusNodeState.UNLOADED
                }
                BusNodeState.FINALIZING -> {
                    debug("Finalizing %s ‹%s›.", mytype, name)
                    delegator?.onFinalizing()
                    state = BusNodeState.FINALIZING
                }
                else -> super.stateChanged(transition)
            }
            transition.succeed()
        } catch (e: Exception) {
            error(e, TEXT_TRANSITION_FAILED, transition.from, transition.unto, context ?: "none")
            transition.fail(e)
        }
    }

    fun waitForMembers(count: Int) {
        memberCheck.acquire(count)
        memberCheck.release(count)
    }

    protected fun unloadMembers(transition: BusNodeStateTransition) {
        accept(CoraControl.createItemSet(this, myMembers.map { (name, node) ->
            node.detach() andThen { accept(RemoveMember(name)) }
        }, this).propagateTo(transition))
    }

    inner class AcceptCandidate(val name: String) : BasicRequest(this@BusHubImpl)
    inner class AcceptMember(val name: String, val node: BusNode) : BasicRequest(this@BusHubImpl)
    inner class RemoveMember(val name: String) : BasicRequest(this@BusHubImpl)
    inner class InternalBusContext(name: String, hubView: BusHubView) : BasicBusContext(name, hubView)

    override fun lookup(name: String) = LookupMemberVoucher(this, name).apply { accept(this) }
    override fun add(name: String, node: BusNode): Request = AddMemberRequest(this, name, node).apply { accept(this) }
    override fun remove(name: String) = RemoveMemberVoucher(this, name).apply { accept(this) }
    override fun replace(name: String, substitute: BusNode): Voucher<BusNode> =
        ReplaceMemberVoucher(this, name, substitute).apply { accept(this) }

    override fun rename(name: String, newName: String): Request =
        RenameMemberRequest(this, name, newName).apply { accept(this) }

    protected open fun onJoining(node: BusNode) {}
    protected open fun onJoined(node: BusNode) {}
    protected open fun onLeaving(member: BusNode) {}
    protected open fun onLeft(member: BusNode) {}
    protected open fun onReady(member: BusNode) {}
    protected open fun onBusy(member: BusNode) {}

    private inner class InternalHusHubView : BusHubView {
        private val my = this@BusHubImpl

        override fun pathOf(name: String): Path = my.path.let { if (it == null) name else namespace.concat(it, name) }
        override fun <D : BusNode> get(type: Class<D>): D? = my.get(type)
        override fun <D : BusNode> get(type: KClass<D>): D? = my.get(type)
        override fun onLeaving(member: BusNode) = my.onLeaving(member)
        override fun onLeft(member: BusNode) = my.onLeft(member)
        override fun onJoining(node: BusNode) = my.onJoining(node)
        override fun onJoined(node: BusNode) = my.onJoined(node)
        override fun onReady(member: BusNode) = my.onReady(member)
        override fun onBusy(member: BusNode) = my.onBusy(member)
    }
}
