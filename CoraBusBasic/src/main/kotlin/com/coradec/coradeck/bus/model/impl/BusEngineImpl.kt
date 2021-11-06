/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusEngineDelegate
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.EngineDelegator
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.text.model.LocalText

@Suppress("UNCHECKED_CAST")
open class BusEngineImpl(
    override val delegator: EngineDelegator? = null
) : BusNodeImpl(delegator), BusEngineDelegate {
    override val upstates: List<BusNodeState> get() = super.upstates + listOf(BusNodeState.STARTING, BusNodeState.STARTED)
    override val downstates: List<BusNodeState> get() = listOf(BusNodeState.STOPPING, BusNodeState.STOPPED) + super.downstates
    override val mytype = "engine"
    override val myType = "Engine"

    override fun stateChanged(transition: BusNodeStateTransition) {
        try {
            val context = transition.context
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here!")
            when (transition.unto) {
                BusNodeState.INITIALIZED -> {
                    debug("Initialized %s ‹%s›.", mytype, name)
                    state = BusNodeState.INITIALIZED
                    delegator?.onInitialized()
                }
                BusNodeState.STARTING -> {
                    debug("Starting %s ‹%s›.", mytype, name)
                    state = BusNodeState.STARTING
                    delegator?.onStarting()
                }
                BusNodeState.STARTED -> {
                    if (delegator != null) CoraControl.IMMEX.execute(delegator!!)
                    debug("Started %s ‹%s›.", mytype, name)
                    state = BusNodeState.STARTED
                    delegator?.onStarted()
                    readify(name)
                }
                BusNodeState.STOPPING -> {
                    busify(name)
                    debug("Stopping %s ‹%s›.", mytype, name)
                    state = BusNodeState.STOPPING
                    delegator?.onStopping()
                }
                BusNodeState.STOPPED -> {
                    debug("Stopped %s ‹%s›.", mytype, name)
                    state = BusNodeState.UNLOADED
                    delegator?.onStopped()
                }
                BusNodeState.FINALIZING -> {
                    debug("Finalizing %s ‹%s›.", mytype, name)
                    state = BusNodeState.FINALIZING
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

    override fun run() = delegator?.run() ?: error(TEXT_NOT_RUNNING)

    companion object {
        private val TEXT_NOT_RUNNING = LocalText("NotRunning")
    }
}
