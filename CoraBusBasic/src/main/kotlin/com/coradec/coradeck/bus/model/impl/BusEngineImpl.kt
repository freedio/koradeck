/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.text.model.LocalText

@Suppress("UNCHECKED_CAST")
open class BusEngineImpl(
    override val delegator: EngineDelegator? = null
) : BusNodeImpl(delegator), BusEngineDelegate {
    override val upstates: List<BusNodeState> get() = super.upstates + listOf(STARTING, STARTED)
    override val downstates: List<BusNodeState> get() = listOf(STOPPING, STOPPED) + super.downstates
    override val mytype = "engine"
    override val myType = "Engine"

    override fun stateChanged(transition: BusNodeStateTransition) {
        try {
            val context = transition.context
            val name = name ?: context?.name ?: throw IllegalStateException("Name must be present here!")
            when (transition.unto) {
                INITIALIZED -> {
                    debug("Initialized %s ‹%s›.", mytype, name)
                    state = INITIALIZED
                    delegator?.onInitialized()
                    transition.succeed()
                }
                STARTING -> {
                    debug("Starting %s ‹%s›.", mytype, name)
                    state = STARTING
                    delegator?.onStarting()
                    transition.succeed()
                }
                STARTED -> {
                    run()
                    debug("Started %s ‹%s›.", mytype, name)
                    state = STARTED
                    delegator?.onStarted()
                    readify(name)
                    transition.succeed()
                }
                STOPPING -> {
                    busify(name)
                    debug("Stopping %s ‹%s›.", mytype, name)
                    stop()
                    state = STOPPING
                    delegator?.onStopping()
                    transition.succeed()
                }
                STOPPED -> {
                    debug("Stopped %s ‹%s›.", mytype, name)
                    state = UNLOADED
                    delegator?.onStopped()
                    transition.succeed()
                }
                FINALIZING -> {
                    debug("Finalizing %s ‹%s›.", mytype, name)
                    state = FINALIZING
                    delegator?.onFinalizing()
                    transition.succeed()
                }
                else -> super.stateChanged(transition)
            }
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
