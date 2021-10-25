/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.model.impl.BasicNodeStateTransition
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin

interface BusNodeStateTransition : Request {
    val from: BusNodeState
    val unto: BusNodeState
    val context: BusContext?

    interface Template {
        val from: BusNodeState
        val unto: BusNodeState
        fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition
    }

    companion object {
        val transitions: Set<Template> get() = setOf(
            ResettingTemplate,
            AttachingTemplate,
            AttachedTemplate,
            InitializingTemplate,
            InitializedTemplate,
            LoadingTemplate,
            LoadedTemplate,
            StartingInitializedTemplate,
            StartingLoadedTemplate,
            StartedTemplate,
            PausingTemplate,
            PausedTemplate,
            ResumingTemplate,
            ResumedTemplate,
            StoppingStartedTemplate,
            StoppingBusyTemplate,
            StoppedTemplate,
            StoppingPausedTemplate,
            UnloadingBusyTemplate,
            UnloadingStoppedTemplate,
            UnloadingLoadedTemplate,
            UnloadedTemplate,
            FinalizingBusyTemplate,
            FinalizingUnloadedTemplate,
            FinalizingInitializedTemplate,
            FinalizedTemplate,
            DetachingFinalizedTemplate,
            DetachingInitializingTemplate,
            DetachingAttachedTemplate,
            DetachingAttachingTemplate,
            DetachingFinalizingTemplate,
            DetachedTemplate
        )

        object ResettingTemplate : Template {
            override val from: BusNodeState = DETACHED
            override val unto: BusNodeState = UNATTACHED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object AttachingTemplate : Template {
            override val from: BusNodeState = UNATTACHED
            override val unto: BusNodeState = ATTACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object AttachedTemplate : Template {
            override val from: BusNodeState = ATTACHING
            override val unto: BusNodeState = ATTACHED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object InitializingTemplate : Template {
            override val from: BusNodeState = ATTACHED
            override val unto: BusNodeState = INITIALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object InitializedTemplate : Template {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = INITIALIZED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object LoadingTemplate : Template {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = LOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object LoadedTemplate : Template {
            override val from: BusNodeState = LOADING
            override val unto: BusNodeState = LOADED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StartingInitializedTemplate : Template {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = STARTING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StartingLoadedTemplate : Template {
            override val from: BusNodeState = LOADED
            override val unto: BusNodeState = STARTING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StartedTemplate : Template {
            override val from: BusNodeState = STARTING
            override val unto: BusNodeState = STARTED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object PausingTemplate : Template {
            override val from: BusNodeState = STARTED
            override val unto: BusNodeState = PAUSING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object PausedTemplate : Template {
            override val from: BusNodeState = PAUSING
            override val unto: BusNodeState = PAUSED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object ResumingTemplate : Template {
            override val from: BusNodeState = PAUSED
            override val unto: BusNodeState = RESUMING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object ResumedTemplate : Template {
            override val from: BusNodeState = RESUMING
            override val unto: BusNodeState = STARTED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppingPausedTemplate : Template {
            override val from: BusNodeState = PAUSED
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppingStartedTemplate : Template {
            override val from: BusNodeState = STARTED
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppingBusyTemplate : Template {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppedTemplate : Template {
            override val from: BusNodeState = STOPPING
            override val unto: BusNodeState = STOPPED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadingStoppedTemplate : Template {
            override val from: BusNodeState = STOPPED
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadingLoadedTemplate : Template {
            override val from: BusNodeState = LOADED
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadingBusyTemplate : Template {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadedTemplate : Template {
            override val from: BusNodeState = UNLOADING
            override val unto: BusNodeState = UNLOADED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizingUnloadedTemplate : Template {
            override val from: BusNodeState = UNLOADED
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizingBusyTemplate : Template {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizingInitializedTemplate : Template {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizedTemplate : Template {
            override val from: BusNodeState = FINALIZING
            override val unto: BusNodeState = FINALIZED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingFinalizedTemplate : Template {
            override val from: BusNodeState = FINALIZED
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingAttachingTemplate : Template {
            override val from: BusNodeState = ATTACHING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingAttachedTemplate : Template {
            override val from: BusNodeState = ATTACHED
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingInitializingTemplate : Template {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingFinalizingTemplate : Template {
            override val from: BusNodeState = FINALIZING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachedTemplate : Template {
            override val from: BusNodeState = DETACHING
            override val unto: BusNodeState = DETACHED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        operator fun invoke(
            origin: Origin,
            from: BusNodeState,
            unto: BusNodeState,
            context: BusContext?
        ): BusNodeStateTransition? = transitions.singleOrNull { it.from == from && it.unto == unto }?.transition(origin, context)
    }
}
