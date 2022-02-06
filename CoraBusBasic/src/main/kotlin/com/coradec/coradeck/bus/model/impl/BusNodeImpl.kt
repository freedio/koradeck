/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.com.AttachRequest
import com.coradec.coradeck.bus.com.DetachRequest
import com.coradec.coradeck.bus.com.NodeStateChangedEvent
import com.coradec.coradeck.bus.com.TransitionTrigger
import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.delegation.BusNodeDelegate
import com.coradec.coradeck.bus.model.delegation.NodeDelegator
import com.coradec.coradeck.bus.trouble.NodeNotAttachedException
import com.coradec.coradeck.bus.trouble.StateUnknownException
import com.coradec.coradeck.bus.trouble.StateUnreachableException
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.*
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.trouble.ViewNotFoundException
import com.coradec.coradeck.session.view.View
import com.coradec.coradeck.text.model.LocalText
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

open class BusNodeImpl(override val delegator: NodeDelegator? = null) : BasicAgent(), BusNodeDelegate {
    private val stateRegistry = CopyOnWriteArraySet<Observer>()
    private val detachForced = AtomicBoolean(false)
    private val myStates = mutableListOf<BusNodeState>(UNATTACHED)
    protected open val mytype = "node"
    protected open val myType = "Node"
    override val states: List<BusNodeState> get() = Collections.unmodifiableList(myStates)
    private val contextPresent = CountDownLatch(1)
    override val ready: Boolean = READY in myStates
    override var context: BusContext? = null
    override val attached: Boolean get() = context != null
    override val path: Path? get() = context?.path
    override val name: String? get() = context?.name
    override val memberView: MemberView get() = memberView(Session.current)
    override var state: BusNodeState
        get() = synchronized(myStates) { myStates.last() }
        set(state) {
            synchronized(myStates) {
                fun invert(i: Int): Int = if (i < 0) -i - 1 else i
                fun addState(newstate: BusNodeState) {
                    myStates.add(invert(Collections.binarySearch(myStates, newstate)), newstate)
                }
                if (state !in myStates) {
                    val event = NodeStateChangedEvent(here, this, myStates.last(), state.apply { addState(this) })
                    stateRegistry.forEach { if (it.notify(event)) stateRegistry.remove(it) }
                }
            }
        }

    protected open val upstates: List<BusNodeState>
        get() = listOf(
            UNATTACHED,
            ATTACHING,
            ATTACHED,
            INITIALIZING,
            INITIALIZED
        )
    protected open val downstates: List<BusNodeState>
        get() = listOf(
            FINALIZING,
            FINALIZED,
            DETACHING,
            DETACHED
        )

    init {
        approve(Trajectory::class)
        route(BusNodeStateTransition::class, ::stateChanged)
        route(TransitionTrigger::class, ::triggerTransition)
        route(AttachRequest::class, ::attach)
        route(DetachRequest::class, ::detach)
    }

    override fun memberView(session: Session): MemberView =
        session.view[this, MemberView::class] ?: InternalMemberView(session).also { session.view[this, MemberView::class] = it }

    protected open fun onAttaching(context: BusContext) {}
    protected open fun onAttached(context: BusContext): Boolean = true
    protected open fun onInitializing() {}
    protected open fun onInitialized(): Boolean = true
    protected open fun onFinalizing() {}
    protected open fun onFinalized(): Boolean = true
    protected open fun onDetaching(forced: Boolean) {}
    protected open fun onDetached(): Boolean = true
    protected open fun onReady() {}
    protected open fun onBusy() {}

    protected open fun stateChanged(transition: BusNodeStateTransition) {
        val ctxt = transition.context
        val name = name ?: ctxt?.name ?: throw IllegalStateException("Name must be present here!")
        try {
            when (val terminalState = transition.unto) {
                ATTACHING -> becomeAttaching(transition, ctxt, name)
                ATTACHED -> becomeAttached(transition, ctxt, name)
                INITIALIZING -> becomeInitializing(transition, name)
                INITIALIZED -> becomeInitialized(transition, name, readify = true)
                FINALIZING -> becomeFinalizing(transition, name, busify = true)
                FINALIZED -> becomeFinalized(transition, name)
                DETACHING -> becomeDetaching(transition, name)
                DETACHED -> becomeDetached(transition, name)
                else -> throw StateUnknownException(terminalState)
            }
        } catch (e: Exception) {
            error(e, TEXT_TRANSITION_FAILED, transition.from, transition.unto, ctxt ?: "none")
            transition.fail(e)
        }
    }

    protected fun becomeAttaching(transition: BusNodeStateTransition, ctxt: BusContext?, name: String) {
        val contxt = ctxt ?: throw IllegalArgumentException("Context not specified!")
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    debug("Attaching %s ‹%s› to context ‹%s›.", mytype, name, contxt.path)
                    this@BusNodeImpl.state = ATTACHING
                }
                FAILED -> {
                    detail("Failed to attach %s ‹%s› to context ‹%s›!", mytype, name, contxt.path)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Attaching %s ‹%s› to context ‹%s› was cancelled!", mytype, name, contxt.path)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            onAttaching(contxt)
            delegator?.onAttaching(contxt)
            contxt.joining(transition.member)
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeAttached(transition: BusNodeStateTransition, ctxt: BusContext?, name: String) {
        val contxt = ctxt ?: throw IllegalArgumentException("Context not specified!")
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    context = contxt
                    this@BusNodeImpl.state = ATTACHING
                    debug("Attached %s ‹%s› to context ‹%s›.", mytype, name, contxt.path)
                }
                FAILED -> {
                    detail("Failed to attach %s ‹%s› to context ‹%s›!", mytype, name, contxt.path)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Attaching %s ‹%s› to context ‹%s› was cancelled!", mytype, name, contxt.path)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            if (onAttached(contxt) && delegator?.onAttached(contxt) != false && contxt.joined(transition.member))
                transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeInitializing(transition: BusNodeStateTransition, name: String) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    debug("Initializing %s ‹%s›.", mytype, name)
                    this@BusNodeImpl.state = INITIALIZING
                }
                FAILED -> {
                    detail("Failed to initialize %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Initializing %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            onInitializing()
            delegator?.onInitializing()
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeInitialized(transition: BusNodeStateTransition, name: String, readify: Boolean) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    this@BusNodeImpl.state = INITIALIZED
                    debug("Initialized %s ‹%s›.", mytype, name)
                }
                FAILED -> {
                    detail("Failed to initialize %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Initializing %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            if (onInitialized() && delegator?.onInitialized() != false) {
                transition.succeed()
                if (readify) readify(name)
            }
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeFinalizing(transition: BusNodeStateTransition, name: String, busify: Boolean) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    debug("Finalizing %s ‹%s›.", mytype, name)
                    this@BusNodeImpl.state = FINALIZING
                }
                FAILED -> {
                    detail("Failed to finalize %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Finalizing %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        if (busify) busify(name)
        try {
            onFinalizing()
            delegator?.onFinalizing()
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeFinalized(transition: BusNodeStateTransition, name: String) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    this@BusNodeImpl.state = FINALIZED
                    debug("Finalized %s ‹%s›.", mytype, name)
                }
                FAILED -> {
                    detail("Failed to finalize %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Finalizing %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            if (onFinalized() && delegator?.onFinalized() != false) transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeDetaching(transition: BusNodeStateTransition, name: String) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    debug("Detaching %s ‹%s›.", mytype, name)
                    this@BusNodeImpl.state = DETACHING
                }
                FAILED -> {
                    detail("Failed to detach %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Detaching %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        val forced = detachForced.getAndSet(true)
        try {
            onDetaching(forced)
            delegator?.onDetaching(forced)
            context?.leaving()
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeDetached(transition: BusNodeStateTransition, name: String) {
        transition.whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    context?.left()
                    context = null
                    this@BusNodeImpl.state = DETACHED
                    debug("Detached %s ‹%s›.", mytype, name)
                }
                FAILED -> {
                    detail("Failed to detach %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                CANCELLED -> {
                    detail("Detaching %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            if (onDetached() && delegator?.onDetached() != false) transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun readify(name: String) {
        debug("%s ‹%s› ready.", myType, name)
        delegator?.onReady()
        state = READY
        context?.ready()
    }

    protected fun busify(name: String) {
        debug("%s ‹%s› busy.", myType, name)
        myStates -= READY
        state = BUSY
        context?.busy()
        delegator?.onBusy()
    }

    private fun attach(request: AttachRequest) {
        if (state == DETACHED) {
            myStates.clear()
            myStates += UNATTACHED
        }
        val context = request.context
        trace("Attach: upstates = %s.", upstates.filter { it !in states })
        accept(Trajectory(this, upstates.filter { it !in states }, context)).content.propagateTo(request)
    }

    private fun detach(request: DetachRequest) {
        val nodeName = name?.let { "«$it»" } ?: "<unknown>"
        if (state == DETACHED) {
            warn(TEXT_NODE_ALREADY_DETACHED, nodeName, request.origin)
            request.succeed()
            return
        }
        if (BUSY in myStates) {
            warn(TEXT_NODE_ALREADY_DETACHING, nodeName, request.origin)
            onState(DETACHED) {
                trace(">>> node %s came down.", nodeName)
                request.succeed()
            }
            return
        }
        trace("Detach: downstates = %s.", downstates.filter { it !in states })
        accept(Trajectory(this, downstates.filter { it !in states })).content.propagateTo(request)
    }

    private fun triggerTransition(trigger: TransitionTrigger) {
        val iterator = trigger.states
        if (iterator.hasNext()) {
            val next = iterator.next()
            trace("%s.%d: next = %s", trigger.shortClassname, trigger.identityHashCode, next)
            accept(BasicNodeStateTransition(this, state, next, trigger.memberView, trigger.context)).content.whenFinished {
                when (state) {
                    SUCCESSFUL -> accept(trigger)
                    FAILED -> trigger.trigger.fail(reason)
                    CANCELLED -> trigger.trigger.cancel(reason)
                    else -> relax()
                }
            }
        } else trigger.trigger.succeed()
    }

    override fun attach(origin: Origin, context: BusContext): AttachRequest = accept(AttachRequest(caller, context)).content
    override fun detach(origin: Origin): DetachRequest = accept(DetachRequest(origin)).content
    override fun renameTo(name: String) {
        debug("Renaming member «%s» to «%s».", this.name ?: "unknown", name)
        context?.renameTo(name) ?: throw NodeNotAttachedException()
    }

    override fun context(timeout: Long, timeoutUnit: TimeUnit): BusContext {
        if (contextPresent.await(timeout, timeoutUnit)) return context!!
        else throw TimeoutException("Context not available within $timeout $timeoutUnit!")
    }

    override fun standby(state: BusNodeState) = standby(Timespan(0, SECONDS), state)
    override fun standby(delay: Timespan) = standby(delay, READY)
    override fun standby(delay: Timespan, state: BusNodeState) {
        val latch = CountDownLatch(1)
        onState(state) { latch.countDown() }
        if (delay.amount == 0L) latch.await() else if (!latch.await(delay.amount, delay.unit)) throw TimeoutException()
    }

    override fun onState(state: BusNodeState, action: BusNode.() -> Unit): Unit = synchronized(myStates) {
        if ((state in upstates || state == READY) && (BUSY in myStates || DETACHED in myStates))
            throw StateUnreachableException(state, this.state)
        if (state in myStates) {
            action.invoke(this@BusNodeImpl)
            return
        }
        stateRegistry.add(object : Observer {
            override fun onNotification(event: Event): Boolean = when (event) {
                is NodeStateChangedEvent -> (event.current == state).also {
                    if (it) {
                        action.invoke(this@BusNodeImpl)
                    }
                }
                else -> false
            }
        })
    }

    fun get(type: Class<*>): MemberView? = context?.get(type)
    fun get(type: KClass<*>): MemberView? = context?.get(type)
    @Suppress("UNCHECKED_CAST")
    private fun <V : View> lookupView(session: Session, type: KClass<V>): V? = when(type) {
        in MemberView::class -> InternalMemberView(session) as V
        // insert more internal types here
        else -> delegator?.getView(session, type)
    }

    override fun toString(): String = when {
        attached && delegator != null -> "$delegator «$name»"
        delegator != null -> delegator.toString()
        attached -> "$classname «$name»"
        else -> super.toString()
    }

    internal inner class Trajectory(
        origin: Origin,
        private val stateList: List<BusNodeState>,
        private val context: BusContext? = null
    ): BasicCommand(origin) {
        override fun execute() {
            if (stateList.isEmpty()) succeed()
            else accept(TransitionTrigger(this@BusNodeImpl, this, stateList.iterator(), context, memberView(session)))
        }
    }

    private inner class InternalMemberView(override val session: Session) : MemberView {
        override fun attach(context: BusContext): Request = this@BusNodeImpl.attach(caller2, context)
        override fun detach(): Request = this@BusNodeImpl.detach(caller2)
        override fun <V : View> lookupView(session: Session, type: KClass<V>): V? = this@BusNodeImpl.lookupView(session, type)
        override fun <V : View> getView(session: Session, type: KClass<V>): V = lookupView(session, type)
            ?: throw ViewNotFoundException(this@BusNodeImpl::class, type)
        override fun toString() = "${this@BusNodeImpl}.member($session)"
    }

    companion object {
        @JvmStatic
        protected val TEXT_TRANSITION_FAILED = LocalText("TransitionFailed3")
        private val TEXT_NODE_ALREADY_DETACHING = LocalText("NodeAlreadyDetaching2")
        private val TEXT_NODE_ALREADY_DETACHED = LocalText("NodeAlreadyDetached2")
    }

}
