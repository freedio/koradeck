/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusEngine
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.model.delegation.BusEngineDelegate
import com.coradec.coradeck.bus.model.delegation.EngineDelegator
import com.coradec.coradeck.com.model.RequestState
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.text.model.LocalText

@Suppress("UNCHECKED_CAST")
open class BusEngineImpl(
    override val delegator: EngineDelegator? = null
) : BusNodeImpl(delegator), BusEngineDelegate {
    override val upstates: List<BusNodeState> get() = super.upstates + listOf(STARTING, STARTED)
    override val downstates: List<BusNodeState> get() = listOf(STOPPING, STOPPED) + super.downstates
    override val mytype = "engine"
    override val myType = "Engine"

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
                INITIALIZED -> becomeInitialized(transition, name, readify = false)
                STARTING -> becomeStarting(transition, name)
                STARTED -> becomeStarted(transition, name, readify = true)
                PAUSING -> becomePausing(transition, name)
                PAUSED -> becomePaused(transition, name)
                RESUMING -> becomeResuming(transition, name)
                STOPPING -> becomeStopping(transition, name, busify = true)
                STOPPED -> becomeStopped(transition, name)
                FINALIZING -> becomeFinalizing(transition, name, busify = false)
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
                    this@BusEngineImpl.state = STARTING
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
                    this@BusEngineImpl.state = STARTED
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
                    this@BusEngineImpl.state = STOPPING
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
                    this@BusEngineImpl.state = STARTED
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
                    alert(TEXT_ENGINE_INTERRUPTED, this)
                } catch (e: Exception) {
                    error(e, TEXT_ENGINE_CRASHED, this)
                    crash()
                } finally {
                    this@BusEngineImpl.detach()
                }
            }
            Thread(::execute, "Bus-%04d".format(BusEngine.ID_ENGINE.incrementAndGet())).apply {
                thread = this
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
        private val TEXT_ENGINE_INTERRUPTED = LocalText("EngineInterrupted1")
        private val TEXT_ENGINE_CRASHED = LocalText("EngineCrashed1")
    }
}
