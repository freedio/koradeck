/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.core.trouble.MultiException
import com.coradec.coradeck.core.util.pretty
import com.coradec.coradeck.ctrl.com.MachineStateChangedEvent
import com.coradec.coradeck.ctrl.com.StateMachineReadynessEvent
import com.coradec.coradeck.ctrl.com.StateTransitionRequest
import com.coradec.coradeck.ctrl.ctrl.StateMachine
import com.coradec.coradeck.ctrl.model.State
import com.coradec.coradeck.ctrl.model.StateTransition
import com.coradec.coradeck.text.model.LocalText
import java.time.Duration
import java.util.concurrent.*

open class BasicStateMachine(initialState: State, override val finalState: State) : BasicAgent(), StateMachine {
    private val states = ArrayList<State>()
    private var state: State = initialState
    protected var machineState: State
        get() = state
        set(value) {
            state = value
        }
    protected val machineStates: List<State> = states
    private var ready: Boolean = false
    private val transitions = CopyOnWriteArraySet<StateTransition>()
    private val stateCallbacks = ConcurrentHashMap<State, CopyOnWriteArrayList<() -> Unit>>()
    private val prison = CopyOnWriteArrayList<Thread>()

    override fun subscribe(notification: Notification<*>) {
        val message = notification.content
        val problems = mutableListOf<Exception>()
        subtrace("%s: processing «%s»", this, message)
        transitions
            .filter { transition ->
                transition.nextState !in states && try {
                    transition.isReadyOn(message)
                } catch (e: Exception) {
                    problems += e
                    prison.forEach { it.interrupt() }
                    false
                }
            }
            .map { transition -> transition.nextState }
//                .printStates()
            .maxByOrNull { it.rank }
            ?.let { newState ->
                val previous = state
                state = newState
                states += newState
                trigger(newState)
                debug("%s: %s", this, newState.name)
                accept(MachineStateChangedEvent(this, previous, state))
                when {
                    newState == finalState && !ready -> {
                        accept(StateMachineReadynessEvent(this, true))
                        ready = true
                    }
                    ready && newState != finalState -> {
                        accept(StateMachineReadynessEvent(this, false))
                        ready = false
                    }
                }
            }
            ?: if (message !is StateTransitionRequest &&
                message !is MachineStateChangedEvent &&
                message !is StateMachineReadynessEvent
            ) super.subscribe(notification)
            else if (problems.isNotEmpty()) error(MultiException(problems), TEXT_DIED, this, state.name)
    }

    final override fun addTransition(transition: StateTransition) {
        transitions += transition
    }

    final override fun removeTransition(transition: StateTransition) {
        transitions -= transition
    }

    override fun whenState(state: State, callback: () -> Unit) {
        stateCallbacks.computeIfAbsent(state) { CopyOnWriteArrayList() } += callback
        if (state in states) trigger(state)
    }

    override fun awaitState(state: State) {
        val latch = CountDownLatch(1)
        whenState(state) { latch.countDown() }
        val then = System.nanoTime()
        prison += Thread.currentThread()
        latch.await()
        prison -= Thread.currentThread()
        val now = System.nanoTime()
        if (now != then) {
            val delta = Duration.ofNanos(now - then)
            debug("%s: waited %s for state «%s».", javaClass.simpleName, delta.pretty, state.name)
        }
    }

    override fun awaitState(state: State, timeout: Duration): Boolean {
        val latch = CountDownLatch(1)
        val then = System.nanoTime()
        whenState(state) { latch.countDown() }
        prison += Thread.currentThread()
        val latchDown = latch.await(timeout.toNanos(), TimeUnit.NANOSECONDS)
        prison -= Thread.currentThread()
        if (latchDown) {
            val now = System.nanoTime()
            if (now != then) {
                val delta = Duration.ofNanos(now - then)
                debug("%s: waited %s for state «%s».", javaClass.simpleName, delta.pretty, state.name)
            }
        }
        return latchDown
    }

    private fun trigger(state: State) {
        stateCallbacks.remove(state)?.forEach { it.invoke() }
    }

    private fun <E : State> List<E>.printStates(): List<E> = also {
        when {
            isEmpty() -> debug("%s: No ready states", this@BasicStateMachine)
            size == 1 -> debug("%s: Choosing next state = %s", this@BasicStateMachine, this[0].name)
            else -> debug("%s: Choosing first of %s", this@BasicStateMachine, this.sortedBy { -it.rank })
        }
    }

    companion object {
        private val TEXT_DIED = LocalText("StateMachineDied")
    }
}
