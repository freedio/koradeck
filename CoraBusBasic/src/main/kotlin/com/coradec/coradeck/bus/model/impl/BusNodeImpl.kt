/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.com.*
import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.trouble.StateUnknownException
import com.coradec.coradeck.bus.trouble.StateUnreachableException
import com.coradec.coradeck.bus.trouble.TransitionNotFoundException
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.swallow
import com.coradec.coradeck.ctrl.ctrl.impl.BasicAgent
import com.coradec.coradeck.dir.model.Path
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
    override val path: Path? get() = context?.path
    override val name: String? get() = context?.name
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

    protected open val upstates: Sequence<BusNodeState>
        get() = sequenceOf(
            UNATTACHED,
            ATTACHING,
            ATTACHED,
            INITIALIZING,
            INITIALIZED
        )
    protected open val downstates: Sequence<BusNodeState>
        get() = sequenceOf(
            FINALIZING,
            FINALIZED,
            DETACHING,
            DETACHED
        )

    private fun transition(initial: BusNodeState, terminal: BusNodeState, context: BusContext?): BusNodeStateTransition =
        BusNodeStateTransition(this, initial, terminal, context) ?: throw TransitionNotFoundException(initial, terminal)

    init {
        route(BusNodeStateTransition::class, ::stateChanged)
        route(TransitionTrigger::class, ::triggerTransition)
        route(AttachRequest::class, ::ignore)
        route(DetachRequest::class, ::ignore)
    }

    private fun ignore(request: Request) = request.swallow()

    private fun triggerTransition(trigger: TransitionTrigger) {
        val context = trigger.context
        val next = with(trigger.states.iterator()) { if (hasNext()) next() else null }
        if (next != null) accept(transition(state, next, context)).andThen { accept(trigger) } else trigger.trigger.succeed()
    }

    protected open fun stateChanged(transition: BusNodeStateTransition) {
        val ctxt = transition.context
        val name = name ?: ctxt?.name ?: throw IllegalStateException("Name must be present here!")
        try {
            when (val terminalState = transition.unto) {
                ATTACHING -> {
                    val contxt = ctxt ?: throw IllegalArgumentException("Context not specified!")
                    debug("Attaching %s ‹%s› to context ‹%s›.", mytype, name, contxt.path)
                    delegator?.onAttaching(contxt)
                    state = ATTACHING
                    contxt.joining(this)
                }
                ATTACHED -> {
                    val contxt = ctxt ?: throw IllegalArgumentException("Context not specified!")
                    delegator?.onAttached(contxt)
                    debug("Attached %s ‹%s› to context ‹%s›.", mytype, name, contxt.path)
                    context = contxt
                    state = ATTACHED
                    contxt.joined(this)
                }
                INITIALIZING -> {
                    debug("Initializing %s ‹%s›.", mytype, name)
                    delegator?.onInitializing()
                    state = INITIALIZING
                }
                INITIALIZED -> {
                    delegator?.onInitialized()
                    debug("Initialized %s ‹%s›.", mytype, name)
                    state = INITIALIZED
                    readify(name)
                }
                FINALIZING -> {
                    busify(name)
                    debug("Finalizing %s ‹%s›.", mytype, name)
                    delegator?.onFinalizing()
                    state = FINALIZING
                }
                FINALIZED -> {
                    delegator?.onFinalized()
                    debug("Finalized %s ‹%s›.", mytype, name)
                    state = FINALIZED
                }
                DETACHING -> {
                    debug("Detaching %s ‹%s›.", mytype, name)
                    delegator?.onDetaching(detachForced.getAndSet(true))
                    state = DETACHING
                    context?.leaving()
                }
                DETACHED -> {
                    delegator?.onDetached()
                    debug("Detached %s ‹%s›.", mytype, name)
                    state = DETACHED
                    context?.left()
                    context = null
                }
                else -> throw StateUnknownException(terminalState)
            }
            transition.succeed()
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

    private fun attachRequest(context: BusContext): Request {
        if (state == DETACHED) {
            myStates.clear()
            myStates += UNATTACHED
        }
        return AttachRequest(this, context).apply {
            accept(Attachment(this@BusNodeImpl, this, upstates.dropWhile { it <= this@BusNodeImpl.state }, context))
        }
    }

    private fun detachRequest(): Request {
        if (state == READY) {
            myStates -= READY
            myStates += BUSY
        }
        return DetachRequest(this).apply {
            accept(Detachment(this@BusNodeImpl, this, downstates.dropWhile { it <= this@BusNodeImpl.state }, null))
        }
    }

    override fun attach(context: BusContext): Request = attachRequest(context)
    override fun detach(): Request = detachRequest()
    override fun leave() = accept(detach()).swallow()
    override fun context(timeout: Long, timeoutUnit: TimeUnit): BusContext {
        if (contextPresent.await(timeout, timeoutUnit)) return context!!
        else throw TimeoutException("Context not available within $timeout $timeoutUnit!")
    }

    override fun standby(state: BusNodeState) = standby(Timespan(0, SECONDS), state)
    override fun standby(delay: Timespan) = standby(delay, READY)
    override fun standby() = standby(READY)
    override fun standby(delay: Timespan, state: BusNodeState) {
        val latch = CountDownLatch(1)
        synchronized(myStates) {
            if ((state in upstates || state == READY) && (BUSY in myStates || DETACHED in myStates))
                throw StateUnreachableException(state)
            if (state in myStates) return
            stateRegistry.add(object : Observer {
                override fun onNotification(event: Event): Boolean = when (event) {
                    is NodeStateChangedEvent ->
                        (event.current == state).also {
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

    fun <D : BusNode> get(type: Class<D>): D? = context?.get(type)
    fun <D : BusNode> get(type: KClass<D>): D? = context?.get(type)

    companion object {
        @JvmStatic
        protected val TEXT_TRANSITION_FAILED = LocalText("TransitionFailed3")
    }

}
