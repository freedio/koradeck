/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusEngine
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.delegation.BusMachineDelegate
import com.coradec.coradeck.bus.model.delegation.MachineDelegator
import com.coradec.coradeck.com.model.RequestState
import com.coradec.coradeck.core.util.relax
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

    protected open fun onStarting() {}
    protected open fun onStarted(): Boolean = true
    protected open fun onPausing() {}
    protected open fun onPaused(): Boolean = true
    protected open fun onResuming() {}
    protected open fun onResumed(): Boolean = true
    protected open fun onStopping() {}
    protected open fun onStopped(): Boolean = true

    override fun stateChanged(transition: BusNodeStateTransition) {
        try {
            val context = transition.context
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here!")
            when (transition.unto) {
                BusNodeState.LOADED -> becomeLoaded(transition, name, readify = false)
                BusNodeState.STARTING -> becomeStarting(transition, name)
                BusNodeState.STARTED -> becomeStarted(transition, name, readify = true)
                BusNodeState.PAUSING -> becomePausing(transition, name)
                BusNodeState.PAUSED -> becomePaused(transition, name)
                BusNodeState.RESUMING -> becomeResuming(transition, name)
                BusNodeState.STOPPING -> becomeStopping(transition, name, busify = true)
                BusNodeState.STOPPED -> becomeStopped(transition, name)
                BusNodeState.UNLOADING -> becomeUnloading(transition, name, busify = false)
                else -> super.stateChanged(transition)
            }
        } catch (e: Exception) {
            error(e, TEXT_TRANSITION_FAILED, transition.from, transition.unto, context ?: "none")
            transition.fail(e)
        }
    }

    protected fun becomeStarting(transition: BusNodeStateTransition, name: String) {
        transition.whenFinished {
            when (state) {
                RequestState.SUCCESSFUL -> {
                    debug("Starting %s ‹%s›.", mytype, name)
                    this@BusMachineImpl.state = BusNodeState.STARTING
                }
                RequestState.FAILED -> {
                    detail("Failed to start %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                RequestState.CANCELLED -> {
                    detail("Starting %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            onStarting()
            delegator?.onStarting()
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeStarted(transition: BusNodeStateTransition, name: String, readify: Boolean) {
        transition.whenFinished {
            when (state) {
                RequestState.SUCCESSFUL -> {
                    this@BusMachineImpl.state = BusNodeState.STARTED
                    debug("%s ‹%s› started.", myType, name)
                }
                RequestState.FAILED -> {
                    detail("Failed to start %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                RequestState.CANCELLED -> {
                    detail("Starting %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            if (onStarted() && delegator?.onStarted() != false) {
                run()
                transition.succeed()
                if (readify) readify(name)
            }
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomePausing(transition: BusNodeStateTransition, name: String) {
        TODO("Not yet implemented")
    }

    protected fun becomePaused(transition: BusNodeStateTransition, name: String) {
        TODO("Not yet implemented")
    }

    protected fun becomeResuming(transition: BusNodeStateTransition, name: String) {
        TODO("Not yet implemented")
    }

    protected fun becomeStopping(transition: BusNodeStateTransition, name: String, busify: Boolean) {
        transition.whenFinished {
            when (state) {
                RequestState.SUCCESSFUL -> {
                    debug("Stopping %s ‹%s›.", mytype, name)
                    this@BusMachineImpl.state = BusNodeState.STOPPING
                }
                RequestState.FAILED -> {
                    detail("Failed to start %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                RequestState.CANCELLED -> {
                    detail("Starting %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            if (busify) busify(name)
            onStopping()
            delegator?.onStopping()
            transition.succeed()
        } catch (e: Exception) {
            error(e)
            transition.fail(e)
        }
    }

    protected fun becomeStopped(transition: BusNodeStateTransition, name: String) {
        transition.whenFinished {
            when (state) {
                RequestState.SUCCESSFUL -> {
                    this@BusMachineImpl.state = BusNodeState.STARTED
                    debug("%s ‹%s› stopped.", myType, name)
                }
                RequestState.FAILED -> {
                    detail("Failed to stop %s ‹%s›!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                RequestState.CANCELLED -> {
                    detail("Stopping %s ‹%s› was cancelled!", mytype, name)
                    if (reason != null) error(reason!!)
                }
                else -> relax()
            }
        }
        try {
            if (onStopped() && delegator?.onStopped() != false) {
                stop()
                transition.succeed()
            }
        } catch (e: Exception) {
            error(e)
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
                } finally {
                    this@BusMachineImpl.detach()
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
