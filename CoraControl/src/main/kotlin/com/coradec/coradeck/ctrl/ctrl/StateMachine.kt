/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.ctrl.model.State
import com.coradec.coradeck.ctrl.model.StateTransition
import java.time.Duration

interface StateMachine {
    /** The state the machine want to reach. */
    val finalState: State

    /** Adds the specified transition to the state machine. */
    fun addTransition(transition: StateTransition)
    /** Removes the specified transition from the state machine. */
    fun removeTransition(transition: StateTransition)
    /** Executes the specified callback when the state machine reaches the specified state. */
    fun whenState(state: State, callback: () -> Unit)
    /** Blocks execution until the specified state has been reached. */
    fun awaitState(state: State)

    /** Blocks execution for at most the specified timeout until the specified state has been reached; reports whether reached. */
    fun awaitState(state: State, timeout: Duration): Boolean
}
