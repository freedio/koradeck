/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusMachineDelegate
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.MachineDelegator
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.module.CoraDir

@Suppress("UNCHECKED_CAST")
open class BusMachineImpl(
    private val delegator: MachineDelegator? = null,
    private val namespace: DirectoryNamespace = CoraDir.rootNamespace
) : BusHubImpl(delegator, namespace), BusMachineDelegate {
    override val upstates: Sequence<BusNodeState> get() = super.upstates + sequenceOf(BusNodeState.STARTING, BusNodeState.STARTED)
    override val downstates: Sequence<BusNodeState> get() = sequenceOf(BusNodeState.STOPPING, BusNodeState.STOPPED) + super.downstates
    override val mytype = "machine"
    override val myType = "Machine"

    override fun stateChanged(transition: BusNodeStateTransition) {
        try {
            val context = transition.context
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here!")
            when (transition.unto) {
                BusNodeState.LOADED -> {
                    delegator?.onLoaded()
                    debug("Loaded %s ‹%s›.", mytype, name)
                    state = BusNodeState.LOADED
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
                    state = BusNodeState.STOPPED
                }
                BusNodeState.UNLOADING -> {
                    debug("Unloading %s ‹%s›.", mytype, name)
                    delegator?.onUnloading()
                    state = BusNodeState.UNLOADING
                    unloadMembers(transition)
                    return // avoid succeeding prematurely
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
