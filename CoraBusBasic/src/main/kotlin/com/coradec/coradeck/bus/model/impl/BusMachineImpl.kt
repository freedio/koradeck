/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.module.CoraDir
import com.coradec.coradeck.text.model.LocalText

@Suppress("UNCHECKED_CAST")
open class BusMachineImpl(
    override val delegator: MachineDelegator? = null,
    namespace: DirectoryNamespace = CoraDir.rootNamespace
) : BusHubImpl(delegator, namespace), BusMachineDelegate {
    override val upstates: List<BusNodeState> get() = super.upstates + listOf(BusNodeState.STARTING, BusNodeState.STARTED)
    override val downstates: List<BusNodeState> get() = listOf(BusNodeState.STOPPING, BusNodeState.STOPPED) + super.downstates
    override val mytype = "machine"
    override val myType = "Machine"

    override fun stateChanged(transition: BusNodeStateTransition) {
        try {
            val context = transition.context
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here!")
            when (transition.unto) {
                BusNodeState.LOADED -> {
                    debug("Loaded %s ‹%s›.", mytype, name)
                    state = BusNodeState.LOADED
                    delegator?.onLoaded()
                }
                BusNodeState.STARTING -> {
                    debug("Starting %s ‹%s›.", mytype, name)
                    state = BusNodeState.STARTING
                    delegator?.onStarting()
                }
                BusNodeState.STARTED -> {
                    run()
                    debug("Started %s ‹%s›.", mytype, name)
                    state = BusNodeState.STARTED
                    delegator?.onStarted()
                    readify(name)
                }
                BusNodeState.STOPPING -> {
                    busify(name)
                    debug("Stopping %s ‹%s›.", mytype, name)
                    stop()
                    state = BusNodeState.STOPPING
                    delegator?.onStopping()
                }
                BusNodeState.STOPPED -> {
                    debug("Stopped %s ‹%s›.", mytype, name)
                    state = BusNodeState.STOPPED
                    delegator?.onStopped()
                }
                BusNodeState.UNLOADING -> {
                    debug("Unloading %s ‹%s›.", mytype, name)
                    state = BusNodeState.UNLOADING
                    delegator?.onUnloading()
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

    override fun crash() {
        context?.crashed()
    }

    override fun run() {
        delegator?.apply {
            fun execute() {
                try {
                    run()
                } catch (e: InterruptedException) {
                    warn(TEXT_ENGINE_INTERRUPTED)
                } catch (e: Exception) {
                    error(e, TEXT_ENGINE_CRASHED)
                    crash()
                }
            }
            Thread(::execute, "Bus-%04d".format(BusEngine.ID_ENGINE.incrementAndGet())).apply {
                if (delegator != null) delegator!!.thread = this
                start()
            }
        } ?: error(TEXT_NOT_RUNNING)
    }

    private fun stop() {
        delegator?.apply {
            thread.interrupt()
        }
    }

    companion object {
        private val TEXT_NOT_RUNNING = LocalText("NotRunning")
        private val TEXT_ENGINE_INTERRUPTED = LocalText("EngineInterrupted")
        private val TEXT_ENGINE_CRASHED = LocalText("EngineCrashed")
    }
}
