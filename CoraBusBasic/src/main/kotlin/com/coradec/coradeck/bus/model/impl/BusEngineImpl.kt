/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusEngineDelegate
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.EngineDelegator

@Suppress("UNCHECKED_CAST")
open class BusEngineImpl(
    private val delegator: EngineDelegator? = null
) : BusNodeImpl(delegator), BusEngineDelegate {
    override val upstates: Sequence<BusNodeState> get() = super.upstates + sequenceOf(BusNodeState.STARTING, BusNodeState.STARTED)
    override val downstates: Sequence<BusNodeState> get() = sequenceOf(BusNodeState.STOPPING, BusNodeState.STOPPED) + super.downstates
    override val mytype = "engine"
    override val myType = "Engine"

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
                BusNodeState.STARTING -> {
                    debug("Starting %s ‹%s›.", mytype, name)
                    delegator?.onStarting()
                    state = BusNodeState.STARTING
                }
                BusNodeState.STARTED -> {
                    delegator?.onStarted()
                    debug("Started %s ‹%s›.", mytype, name)
                    state = BusNodeState.STARTED
                    readify(name)
                }
                BusNodeState.STOPPING -> {
                    busify(name)
                    debug("Stopping %s ‹%s›.", mytype, name)
                    delegator?.onStopping()
                    state = BusNodeState.STOPPING
                }
                BusNodeState.STOPPED -> {
                    delegator?.onStopped()
                    debug("Stopped %s ‹%s›.", mytype, name)
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
}
