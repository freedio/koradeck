/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.delegation.BusHubDelegate
import com.coradec.coradeck.bus.model.delegation.HubDelegator
import com.coradec.coradeck.bus.trouble.MemberNotFoundException
import com.coradec.coradeck.bus.view.BusHubView
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.BasicVoucher
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority.B1
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.module.CoraControl.createRequestList
import com.coradec.coradeck.ctrl.module.CoraControl.createRequestSet
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.module.CoraDir
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class BusHubImpl(
    override val delegator: HubDelegator? = null,
    private val namespace: DirectoryNamespace = CoraDir.rootNamespace
) : BusNodeImpl(delegator), BusHubDelegate {
    override val members: Voucher<Map<String, MemberView>> get() = MemberVoucher(this).apply { accept(this) }
    override val names: Voucher<Set<String>> get() = NamesVoucher(this).apply { accept(this) }
    private val myMembers = mutableMapOf<String, MemberView>()
    private val candidates = mutableMapOf<AddMemberRequest, MemberView>()
    override val mytype = "hub"
    override val myType = "Hub"
    override val upstates: List<BusNodeState> get() = super.upstates + listOf(LOADING, LOADED)
    override val downstates: List<BusNodeState> get() = listOf(UNLOADING, UNLOADED) + super.downstates

    init {
        route(LookupMemberVoucher::class, ::lookupMember)
        route(ContainsMemberVoucher::class, ::containsMember)
        route(AddMemberRequest::class, ::addMember)
        route(AddCandidateRequest::class, ::addCandidate)
        route(AcceptMemberRequest::class, ::acceptMember)
        route(AcceptCandidateRequest::class, ::acceptCandidate)
        route(RemoveMemberVoucher::class, ::removeMember)
        route(ReplaceMemberVoucher::class, ::replaceMember)
        route(RenameMemberRequest::class, ::renameMember)
        route(MemberVoucher::class, ::members)
        route(NamesVoucher::class, ::names)
        route(UnlinkMemberRequest::class, ::unlinkMember)
    }

    protected open fun onLoading() {}
    protected open fun onLoaded(): Boolean = true
    protected open fun onUnloading() {}
    protected open fun onUnloaded(): Boolean = true
    protected open fun onJoining(name: String, node: MemberView) {}
    protected open fun onJoined(name: String, node: MemberView): Boolean = true
    protected open fun onLeaving(name: String, node: MemberView) {}
    protected open fun onLeft(name: String, node: MemberView): Boolean = true

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

    private fun addCandidate(request: AddCandidateRequest) {
        val name = request.name
        val candEntry = candidates.mapNotNull { if (it.key.name == name) it else null }.singleOrNull()
            ?: throw IllegalStateException("Expected exactly one candidate with name ‹$name›, but got $candidates")
        val candidate = candEntry.value
        val session = request.session
        candidate.attach(InternalBusContext(session, name, InternalBusHubView(session))).whenFinished {
            when (this.state) {
                SUCCESSFUL -> {
                    debug("Candidate «%s» attached.", name)
                    accept(AcceptCandidateRequest(this@BusHubImpl, name, candidate).propagateTo(request))
                }
                FAILED -> request.fail(reason)
                CANCELLED -> request.cancel(reason)
                else -> relax()
            }
        }
    }

    private fun acceptCandidate(request: AcceptCandidateRequest) {
        val name = request.name
        val candidate = request.node
        val candEntry = candidates.mapNotNull { if (it.key.name == name) it else null }.singleOrNull()
            ?: throw IllegalStateException("Expected exactly one candidate with name ‹$name›, but got $candidates")
        myMembers[name] = candidate
        candidates -= candEntry.key.apply { succeed() }
        debug("Candidate ‹%s› accepted as member.", name)
        request.succeed()
    }

    private fun acceptMember(request: AcceptMemberRequest) {
        val name = request.name
        val node = request.node
        debug("Accepting member ‹%s› as «%s»", node, name)
        myMembers[name] = node
        request.succeed()
    }

    private fun addMember(request: AddMemberRequest) {
        val name = request.name
        val node = request.node
        trace("Received request to add member ‹%s› as «%s».", node, name)
        try {
            if (loading) {
                debug("Hub already attached -> attaching member «%s» directly.", name)
                val session = request.session
                node.attach(InternalBusContext(session, name, InternalBusHubView(session))).propagateTo(request) andThen {
                    debug("Attached member ‹%s› as «%s».", node, name)
                }
            } else {
                debug("Hub not (yet) attached -> adding node «%s» as candidate.", name)
                candidates[request] = node
            }
            trace("Candidates: %s, Members: %s.", candidates.entries, myMembers.entries)
        } catch (e: Exception) {
            error(e)
            request.fail(e)
        }
    }

    private fun removeMember(voucher: RemoveMemberVoucher) {
        val name = voucher.name
        myMembers[name]?.apply {
            voucher.value = this
            detach().propagateTo(voucher)
        } ?: voucher.fail(MemberNotFoundException(name)/*.apply { error(this) }*/)
    }

    private fun unlinkMember(request: UnlinkMemberRequest) {
        val name = request.name
        debug("Unlinking member ‹%s›", name)
        if (myMembers.remove(name) == null) warn(TEXT_MEMBER_ALREADY_GONE, name)
        request.succeed()
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
                    AcceptMemberRequest(here, name, node),
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
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here! Transition: $transition")
            when (transition.unto) {
                INITIALIZED -> becomeInitialized(transition, name, readify = false)
                LOADING -> becomeLoading(transition, name)
                LOADED -> becomeLoaded(transition, name, readify = true)
                UNLOADING -> becomeUnloading(transition, name, busify = true)
                UNLOADED -> becomeUnloaded(transition, name)
                FINALIZING -> becomeFinalizing(transition, name, busify = false)
                else -> super.stateChanged(transition)
            }
        } catch (e: Exception) {
            error(e, TEXT_TRANSITION_FAILED, transition.from, transition.unto, context ?: "none")
            transition.fail(e)
        }
    }

    protected fun becomeLoading(transition: BusNodeStateTransition, name: String) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    debug("Loading %s ‹%s›.", mytype, name)
                    this@BusHubImpl.state = LOADING
                }
                FAILED -> {
                    detail("Failed to load %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Loading %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            onLoading()
            delegator?.onLoading()
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeLoaded(transition: BusNodeStateTransition, name: String, readify: Boolean) {
        fun endLoaded() {
            try {
                if (onLoaded() && delegator?.onLoaded() != false) {
                    transition.succeed()
                    if (readify) readify(name)
                }
            } catch (e: Exception) {
                error(e)
                transition.fail(e)
            }
        }
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    this@BusHubImpl.state = LOADED
                    debug("Loaded %s ‹%s›.", mytype, name)
                }
                FAILED -> {
                    detail("Failed to load %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Loading %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        if (candidates.isEmpty()) endLoaded()
        else accept(createRequestSet(this, candidates.keys.map { AddCandidateRequest(here, it.name) }, this).whenFinished {
            when (state) {
                SUCCESSFUL -> endLoaded()
                FAILED -> {
                    detail("Failed to add members of %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                    transition.fail(reason)
                }
                CANCELLED -> {
                    detail("Adding members to %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                    transition.fail(reason)
                }
                else -> relax()
            }
        })
    }

    protected fun becomeUnloading(transition: BusNodeStateTransition, name: String, busify: Boolean) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    debug("Unloading %s ‹%s›.", mytype, name)
                    this@BusHubImpl.state = LOADING
                }
                FAILED -> {
                    detail("Failed to unload %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Unloading %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        if (busify) busify(name)
        try {
            onUnloading()
            delegator?.onUnloading()
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeUnloaded(transition: BusNodeStateTransition, name: String) {
        fun endUnload() {
            try {
                if (onUnloaded() && delegator?.onUnloaded() != false) {
                    transition.succeed()
                }
            } catch (e: Exception) {
                error(e)
                transition.fail(e)
            }
        }
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    this@BusHubImpl.state = UNLOADED
                    debug("Unloaded %s ‹%s›.", mytype, name)
                }
                FAILED -> {
                    detail("Failed to unload %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Unloading %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        if (myMembers.isEmpty()) endUnload()
        else accept(
            createRequestSet(this, myMembers.keys.map { RemoveMemberVoucher(here, it) }, this).whenFinished {
                when (state) {
                    SUCCESSFUL -> endUnload()
                    FAILED -> {
                        detail("Failed to remove members of %s ‹%s›!", mytype, name)
                        if (reason != null) error(reason!!)
                        transition.fail(reason)
                    }
                    CANCELLED -> {
                        detail("Removing members of %s ‹%s› was cancelled!", mytype, name)
                        if (reason != null) error(reason!!)
                        transition.fail(reason)
                    }
                    else -> relax()
                }
            })
    }

    inner class InternalBusContext(session: Session, name: String, hubView: BusHubView) : BasicBusContext(session, name, hubView)

    override fun lookup(name: String): Voucher<MemberView> = accept(LookupMemberVoucher(this, name)).content
    override fun contains(name: String) = accept(ContainsMemberVoucher(this, name)).content as Voucher<Boolean>
    override fun add(name: String, node: MemberView): Request = accept(AddMemberRequest(this, name, node)).content
    override fun remove(name: String): Voucher<MemberView> = accept(RemoveMemberVoucher(this, name)).content
    override fun rename(name: String, newName: String): Request = accept(RenameMemberRequest(this, name, newName)).content
    override fun replace(name: String, substitute: MemberView): Voucher<MemberView> =
        accept(ReplaceMemberVoucher(this, name, substitute)).content

    protected open fun onJoining(node: MemberView) {}
    protected open fun onJoined(node: MemberView): Boolean = true
    protected open fun onLeaving(member: MemberView) {}
    protected open fun onLeft(member: MemberView): Boolean = true
    protected open fun onReady(member: MemberView) {}
    protected open fun onBusy(member: MemberView) {}
    protected open fun onCrashed(member: MemberView) {
        member.detach()
    }

    protected open fun link(name: String, member: MemberView) {
        accept(AcceptMemberRequest(here, name, member))
    }

    protected open fun unlink(name: String) {
        accept(UnlinkMemberRequest(here, name))
    }

    internal class LookupMemberVoucher(origin: Origin, val name: String) : BasicVoucher<MemberView>(origin)
    private class ContainsMemberVoucher(origin: Origin, val name: String) : BasicVoucher<Boolean>(origin)
    private class MemberVoucher(origin: Origin) : BasicVoucher<Map<String, MemberView>>(origin)
    internal class AddMemberRequest(origin: Origin, val name: String, val node: MemberView) : BasicRequest(origin, priority = B1)
    private class AddCandidateRequest(origin: Origin, val name: String) : BasicRequest(origin, priority = B1)
    private class AcceptCandidateRequest(origin: Origin, val name: String, val node: MemberView) :
        BasicRequest(origin, priority = B1)

    internal class AcceptMemberRequest(origin: Origin, val name: String, val node: MemberView) : BasicRequest(origin, priority = B1)
    internal class RemoveMemberVoucher(origin: Origin, val name: String) : BasicVoucher<MemberView>(origin)
    private class RenameMemberRequest(origin: Origin, val name: String, val newName: String) : BasicRequest(origin)
    private class ReplaceMemberVoucher(origin: Origin, val name: String, val substitute: MemberView) :
        BasicVoucher<MemberView>(origin)

    internal class UnlinkMemberRequest(origin: Origin, val name: String) : BasicRequest(origin)
    private class NamesVoucher(origin: Origin) : BasicVoucher<Set<String>>(origin)

    private inner class InternalBusHubView(override val session: Session) : BusHubView {
        private val my = this@BusHubImpl

        override fun pathOf(name: String): Path = my.path.let { if (it == null) name else namespace.concat(it, name) }
        override fun get(type: Class<*>): MemberView? = my.delegator?.node
        override fun get(type: KClass<*>): MemberView? = my.delegator?.node
        override fun onLeaving(member: MemberView) = my.onLeaving(member)
        override fun onLeft(member: MemberView): Boolean = my.onLeft(member)
        override fun onJoining(node: MemberView) = my.onJoining(node)
        override fun onJoined(node: MemberView): Boolean = my.onJoined(node)
        override fun onReady(member: MemberView) = my.onReady(member)
        override fun onBusy(member: MemberView) = my.onBusy(member)
        override fun onCrashed(member: MemberView) = my.onCrashed(member)
        override fun link(name: String, node: MemberView) = my.link(name, node)
        override fun unlink(name: String) = my.unlink(name)
        override fun rename(name: String, newName: String) = my.rename(name, newName).standby().swallow()
    }

    companion object {
        private val TEXT_MEMBER_ALREADY_GONE = LocalText("MemberAlreadyGone1")
    }
}
