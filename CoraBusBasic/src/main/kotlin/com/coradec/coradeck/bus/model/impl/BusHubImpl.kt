/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.trouble.MemberNotFoundException
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.BasicVoucher
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.module.CoraControl.createItemSet
import com.coradec.coradeck.ctrl.module.CoraControl.createRequestList
import com.coradec.coradeck.ctrl.module.CoraControl.createRequestSet
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDir
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class BusHubImpl(
    override val delegator: HubDelegator? = null,
    private val namespace: DirectoryNamespace = CoraDir.rootNamespace
) : BusNodeImpl(delegator), BusHubDelegate {
    override val members: Voucher<Map<String, BusNode>> get() = MemberVoucher(this).apply { accept(this) }
    override val names: Voucher<Set<String>> get() = NamesVoucher(this).apply { accept(this) }
    private val myMembers = mutableMapOf<String, BusNode>()
    private val candidates = mutableMapOf<AddMemberRequest, BusNode>()
    override val mytype = "hub"
    override val myType = "Hub"
    override val upstates: List<BusNodeState> get() = super.upstates + listOf(LOADING, LOADED)
    override val downstates: List<BusNodeState> get() = listOf(UNLOADING, UNLOADED) + super.downstates

    init {
        route(LookupMemberVoucher::class, ::lookupMember)
        route(ContainsMemberVoucher::class, ::containsMember)
        route(AddMemberRequest::class, ::addMember)
        route(AcceptMemberRequest::class, ::acceptMember)
        route(RemoveMemberVoucher::class, ::removeMember)
        route(ReplaceMemberVoucher::class, ::replaceMember)
        route(RenameMemberRequest::class, ::renameMember)
        route(AcceptCandidateRequest::class, ::acceptCandidate)
        route(MemberVoucher::class, ::members)
        route(NamesVoucher::class, ::names)
        route(UnlinkMemberRequest::class, ::unlinkMember)
    }

    private fun lookupMember(voucher: LookupMemberVoucher) {
        val memberName = voucher.name
        myMembers[memberName]?.apply {
            voucher.value = this
            voucher.succeed()
        } ?: voucher.fail(MemberNotFoundException(memberName))
    }

    private fun containsMember(voucher: ContainsMemberVoucher) {
        myMembers.contains(voucher.name).apply {
            voucher.value = this
            voucher.succeed()
        }
    }

    private fun members(voucher: MemberVoucher) {
        voucher.value = myMembers
        voucher.succeed()
    }

    private fun names(voucher: NamesVoucher) {
        voucher.value = myMembers.keys
        voucher.succeed()
    }

    private fun acceptCandidate(request: AcceptCandidateRequest) {
        val name = request.name
        val candEntry = candidates.mapNotNull { if (it.key.name == name) it else null }.singleOrNull()
            ?: throw IllegalStateException("Expected exactly one candidate with name ‹$name›, but got $candidates")
        val candidate = candEntry.value
        myMembers[name] = candidate
        candidates -= candEntry.key.apply { succeed() }
        debug("Candidate ‹%s› accepted as member.", name)
        request.succeed()
    }

    private fun acceptMember(request: AcceptMemberRequest) {
        val name = request.name
        val node = request.node
        debug("Waiting for member ‹%s› to become ready", name)
        debug("Accepting member ‹%s› as «%s»", node, name)
        myMembers[name] = node
        request.succeed()
    }

    private fun addMember(request: AddMemberRequest) {
        try {
            val name = request.name
            val node = request.node
            if (attached)
                accept(
                    createRequestList(
                        here,
                        node.attach(InternalBusContext(name, InternalBusHubView())),
                        AcceptMemberRequest(here, name, node),
                        processor = this
                    )
                ).content.propagateTo(request)
            else {
                candidates[request] = node
                request.succeed()
            }
            trace("Candidates: %s, Members: %s.", candidates.entries, myMembers.entries)
        } catch (e: Exception) {
            request.fail(e)
        }
    }

    private fun removeMember(voucher: RemoveMemberVoucher) {
        val name = voucher.name
        myMembers[name]?.apply {
            voucher.value = this
            detach().propagateTo(voucher)
        } ?: voucher.fail(MemberNotFoundException(name))
    }

    private fun unlinkMember(request: UnlinkMemberRequest) {
        val name = request.name
        debug("Unlinking member ‹%s›", name)
        if (myMembers.remove(name) == null) request.fail(MemberNotFoundException(name)) else request.succeed()
    }

    private fun replaceMember(voucher: ReplaceMemberVoucher) {
        val name = voucher.name
        val node = voucher.substitute
        myMembers[name]?.let {
            voucher.value = it
            accept(
                createRequestList(
                    here,
                    RemoveMemberVoucher(here, name),
                    AddMemberRequest(here, name, node),
                    processor = this
                ) propagateTo voucher
            )
        } ?: voucher.fail(MemberNotFoundException(name))
    }

    private fun renameMember(request: RenameMemberRequest) {
        val name0 = request.name
        val name1 = request.newName
        myMembers.remove(name0)?.apply {
            myMembers[name1] = this
            renameTo(name1)
            request.succeed()
        } ?: request.fail(MemberNotFoundException(name0))
    }

    override fun stateChanged(transition: BusNodeStateTransition) {
        try {
            val context = transition.context
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here!")
            when (transition.unto) {
                INITIALIZED -> {
                    debug("Initialized %s ‹%s›.", mytype, name)
                    state = INITIALIZED
                    delegator?.onInitialized()
                }
                LOADING -> {
                    debug("Loading %s ‹%s›.", mytype, name)
                    state = LOADING
                    delegator?.onLoading()
                    accept(
                        createItemSet(this, candidates.map { (addMemReq, node) ->
                            val key = addMemReq.name
                            node.attach(InternalBusContext(key, InternalBusHubView())) andThen {
                                accept(
                                    AcceptCandidateRequest(
                                        here,
                                        key
                                    )
                                )
                            }
                        }, this).propagateTo(transition)
                    )
                    return // avoid succeeding prematurely
                }
                LOADED -> {
                    debug("Loaded %s ‹%s›.", mytype, name)
                    state = LOADED
                    delegator?.onLoaded()
                    readify(name)
                }
                UNLOADING -> {
                    busify(name)
                    debug("Unloading %s ‹%s›.", mytype, name)
                    state = UNLOADING
                    delegator?.onUnloading()
                    unloadMembers(transition)
                    return // avoid succeeding prematurely
                }
                UNLOADED -> {
                    debug("Unloaded %s ‹%s›.", mytype, name)
                    state = UNLOADED
                    delegator?.onUnloaded()
                }
                FINALIZING -> {
                    debug("Finalizing %s ‹%s›.", mytype, name)
                    state = FINALIZING
                    delegator?.onFinalizing()
                }
                else -> super.stateChanged(transition)
            }
            transition.succeed()
        } catch (e: Exception) {
            error(e, TEXT_TRANSITION_FAILED, transition.from, transition.unto, context ?: "none")
            transition.fail(e)
        }
    }

    protected fun unloadMembers(transition: BusNodeStateTransition) {
        debug("Unloading %d member(s): %s", myMembers.size, myMembers.values)
//        val memberCount = myMembers.size
        accept(
            createRequestSet(
                this,
                myMembers.map { (_, node) -> node.detach() },
                this
            ).propagateTo(transition)
        )
//        memberCheck.acquire(memberCount)
    }

    inner class InternalBusContext(name: String, hubView: BusHubView) : BasicBusContext(name, hubView)

    override fun lookup(name: String) = LookupMemberVoucher(this, name).apply { accept(this) } as Voucher<BusNode>
    override fun contains(name: String) = ContainsMemberVoucher(this, name).apply { accept(this) } as Voucher<Boolean>
    override fun add(name: String, node: BusNode): Request = AddMemberRequest(this, name, node).apply { accept(this) }
    override fun remove(name: String) = RemoveMemberVoucher(this, name).apply { accept(this) } as Voucher<BusNode>
    override fun rename(name: String, newName: String): Request = RenameMemberRequest(this, name, newName).apply { accept(this) }

    override fun replace(name: String, substitute: BusNode): Voucher<BusNode> =
        ReplaceMemberVoucher(this, name, substitute).apply { accept(this) }

    protected open fun onJoining(node: BusNode) {}
    protected open fun onJoined(node: BusNode) {}
    protected open fun onLeaving(member: BusNode) {}
    protected open fun onLeft(member: BusNode) {}
    protected open fun onReady(member: BusNode) {}
    protected open fun onBusy(member: BusNode) {}
    protected open fun onCrashed(member: BusNode) { leave() }
    protected open fun link(name: String, member: BusNode) {}
    protected open fun unlink(name: String) {
        accept(UnlinkMemberRequest(here, name))
    }

    private class LookupMemberVoucher(origin: Origin, val name: String) : BasicVoucher<BusNode>(origin)
    private class ContainsMemberVoucher(origin: Origin, val name: String) : BasicVoucher<Boolean>(origin)
    private class MemberVoucher(origin: Origin) : BasicVoucher<Map<String, BusNode>>(origin)
    internal class AddMemberRequest(origin: Origin, val name: String, val node: BusNode) : BasicRequest(origin)
    private class AcceptCandidateRequest(origin: Origin, val name: String) : BasicRequest(origin)
    private class AcceptMemberRequest(origin: Origin, val name: String, val node: BusNode) : BasicRequest(origin)
    private class RemoveMemberVoucher(origin: Origin, val name: String) : BasicVoucher<BusNode>(origin)
    private class RenameMemberRequest(origin: Origin, val name: String, val newName: String) : BasicRequest(origin)
    private class ReplaceMemberVoucher(origin: Origin, val name: String, val substitute: BusNode) : BasicVoucher<BusNode>(origin)
    internal class UnlinkMemberRequest(origin: Origin, val name: String) : BasicRequest(origin)
    private class NamesVoucher(origin: Origin) : BasicVoucher<Set<String>>(origin)

    private inner class InternalBusHubView : BusHubView {
        private val my = this@BusHubImpl

        override fun pathOf(name: String): Path = my.path.let { if (it == null) name else namespace.concat(it, name) }
        override fun <D : BusNode> get(type: Class<D>): D? = my.delegator?.node as D?
        override fun <D : BusNode> get(type: KClass<D>): D? = my.delegator?.node as D?
        override fun onLeaving(member: BusNode) = my.onLeaving(member)
        override fun onLeft(member: BusNode) = my.onLeft(member)
        override fun onJoining(node: BusNode) = my.onJoining(node)
        override fun onJoined(node: BusNode) = my.onJoined(node)
        override fun onReady(member: BusNode) = my.onReady(member)
        override fun onBusy(member: BusNode) = my.onBusy(member)
        override fun onCrashed(member: BusNode) = my.onCrashed(member)
        override fun link(name: String, node: BusNode) = my.link(name, node)
        override fun unlink(name: String) = my.unlink(name)
    }
}
