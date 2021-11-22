/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.com.AttachRequest
import com.coradec.coradeck.bus.com.DetachRequest
import com.coradec.coradeck.bus.com.NodeStateChangedEvent
import com.coradec.coradeck.bus.com.TransitionTrigger
import com.coradec.coradeck.bus.model.BusNodeDelegate
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.NodeDelegator
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

    override fun memberView(session: Session): MemberView =
        session.view[this, MemberView::class] ?: InternalMemberView(session).also { session.view[this, MemberView::class] = it }

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

    protected open fun stateChanged(transition: BusNodeStateTransition) {
        val ctxt = transition.context
        val name = name ?: ctxt?.name ?: throw IllegalStateException("Name must be present here!")
        try {
            when (val terminalState = transition.unto) {
                ATTACHING -> {
                    val contxt = ctxt ?: throw IllegalArgumentException("Context not specified!")
                    debug("Attaching %s ‹%s› to context ‹%s›.", mytype, name, contxt.path)
                    state = ATTACHING
                    delegator?.onAttaching(contxt)
                    contxt.joining(transition.member)
                    transition.succeed()
                }
                ATTACHED -> {
                    val contxt = ctxt ?: throw IllegalArgumentException("Context not specified!")
                    state = ATTACHED
                    debug("Attached %s ‹%s› to context ‹%s›.", mytype, name, contxt.path)
                    context = contxt
                    delegator?.onAttached(contxt)
                    contxt.joined(transition.member)
                    transition.succeed()
                }
                INITIALIZING -> {
                    debug("Initializing %s ‹%s›.", mytype, name)
                    state = INITIALIZING
                    delegator?.onInitializing()
                    transition.succeed()
                }
                INITIALIZED -> {
                    state = INITIALIZED
                    delegator?.onInitialized()
                    debug("Initialized %s ‹%s›.", mytype, name)
                    readify(name)
                    transition.succeed()
                }
                FINALIZING -> {
                    busify(name)
                    debug("Finalizing %s ‹%s›.", mytype, name)
                    state = FINALIZING
                    delegator?.onFinalizing()
                    transition.succeed()
                }
                FINALIZED -> {
                    state = FINALIZED
                    delegator?.onFinalized()
                    debug("Finalized %s ‹%s›.", mytype, name)
                    transition.succeed()
                }
                DETACHING -> {
                    debug("Detaching %s ‹%s›.", mytype, name)
                    state = DETACHING
                    delegator?.onDetaching(detachForced.getAndSet(true))
                    context?.leaving()
                    transition.succeed()
                }
                DETACHED -> {
                    context?.left()
                    context = null
                    state = DETACHED
                    delegator?.onDetached()
                    transition.succeed()
                    debug("Detached %s ‹%s›.", mytype, name)
                }
                else -> throw StateUnknownException(terminalState)
            }
        } catch (e: Exception) {
            error(e, TEXT_TRANSITION_FAILED, transition.from, transition.unto, ctxt ?: "none")
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
        if (state == READY) {
            myStates -= READY
            myStates += BUSY
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

    override fun attach(context: BusContext): AttachRequest = accept(AttachRequest(caller, context)).content
    override fun detach(): DetachRequest = accept(DetachRequest(caller)).content
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
        synchronized(myStates) {
            if ((state in upstates || state == READY) && (BUSY in myStates || DETACHED in myStates))
                throw StateUnreachableException(state, this.state)
            if (state in myStates) return
            stateRegistry.add(object : Observer {
                override fun onNotification(event: Event): Boolean = when (event) {
                    is NodeStateChangedEvent -> (event.current == state).also {
                        if (it) {
                            latch.countDown()
                        }
                    }
                    else -> false
                }
            })
        }
        if (delay.amount == 0L) latch.await() else if (!latch.await(delay.amount, delay.unit)) throw TimeoutException()
    }

    fun get(type: Class<*>): MemberView? = context?.get(type)
    fun get(type: KClass<*>): MemberView? = context?.get(type)
    @Suppress("UNCHECKED_CAST")
    private fun <V : View> lookupView(session: Session, type: KClass<V>): V? = when(type) {
        in MemberView::class -> InternalMemberView(session) as V
        // insert more internal types here
        else -> delegator?.getView(session, type)
    }

    override fun toString(): String = if (attached) "%s «$name»".format(this.classname) else super.toString()

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

    private inner class InternalMemberView(session: Session) : AbstractMemberView(session) {
        override fun attach(context: BusContext): Request = this@BusNodeImpl.attach(context)
        override fun detach(): Request = this@BusNodeImpl.detach()
        override fun <V : View> lookupView(session: Session, type: KClass<V>): V? = this@BusNodeImpl.lookupView(session, type)
        override fun <V : View> getView(session: Session, type: KClass<V>): V = lookupView(session, type)
            ?: throw ViewNotFoundException(this@BusNodeImpl::class, type)
    }

    companion object {
        @JvmStatic
        protected val TEXT_TRANSITION_FAILED = LocalText("TransitionFailed3")
    }

}
