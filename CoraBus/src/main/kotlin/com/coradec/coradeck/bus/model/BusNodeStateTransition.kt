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

    interface Templet {
        val from: BusNodeState
        val unto: BusNodeState
        fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition
    }

    companion object {
        private val transitions: Set<Templet> get() = setOf(
            ResettingTemplet,
            AttachingTemplet,
            AttachedTemplet,
            InitializingTemplet,
            InitializedTemplet,
            LoadingTemplet,
            LoadedTemplet,
            StartingInitializedTemplet,
            StartingLoadedTemplet,
            StartedTemplet,
            PausingTemplet,
            PausedTemplet,
            ResumingTemplet,
            ResumedTemplet,
            StoppingStartedTemplet,
            StoppingBusyTemplet,
            StoppedTemplet,
            StoppingPausedTemplet,
            UnloadingBusyTemplet,
            UnloadingStoppedTemplet,
            UnloadingLoadingTemplet,
            UnloadingLoadedTemplet,
            UnloadedTemplet,
            FinalizingBusyTemplet,
            FinalizingUnloadedTemplet,
            FinalizingInitializingTemplet,
            FinalizingInitializedTemplet,
            FinalizedTemplet,
            DetachingFinalizedTemplet,
            DetachingInitializingTemplet,
            DetachingAttachedTemplet,
            DetachingAttachingTemplet,
            DetachingFinalizingTemplet,
            DetachedTemplet
        )

        object ResettingTemplet : Templet {
            override val from: BusNodeState = DETACHED
            override val unto: BusNodeState = UNATTACHED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object AttachingTemplet : Templet {
            override val from: BusNodeState = UNATTACHED
            override val unto: BusNodeState = ATTACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object AttachedTemplet : Templet {
            override val from: BusNodeState = ATTACHING
            override val unto: BusNodeState = ATTACHED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object InitializingTemplet : Templet {
            override val from: BusNodeState = ATTACHED
            override val unto: BusNodeState = INITIALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object InitializedTemplet : Templet {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = INITIALIZED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object LoadingTemplet : Templet {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = LOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object LoadedTemplet : Templet {
            override val from: BusNodeState = LOADING
            override val unto: BusNodeState = LOADED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StartingInitializedTemplet : Templet {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = STARTING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StartingLoadedTemplet : Templet {
            override val from: BusNodeState = LOADED
            override val unto: BusNodeState = STARTING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StartedTemplet : Templet {
            override val from: BusNodeState = STARTING
            override val unto: BusNodeState = STARTED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object PausingTemplet : Templet {
            override val from: BusNodeState = STARTED
            override val unto: BusNodeState = PAUSING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object PausedTemplet : Templet {
            override val from: BusNodeState = PAUSING
            override val unto: BusNodeState = PAUSED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object ResumingTemplet : Templet {
            override val from: BusNodeState = PAUSED
            override val unto: BusNodeState = RESUMING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object ResumedTemplet : Templet {
            override val from: BusNodeState = RESUMING
            override val unto: BusNodeState = STARTED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppingPausedTemplet : Templet {
            override val from: BusNodeState = PAUSED
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppingStartedTemplet : Templet {
            override val from: BusNodeState = STARTED
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppingBusyTemplet : Templet {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object StoppedTemplet : Templet {
            override val from: BusNodeState = STOPPING
            override val unto: BusNodeState = STOPPED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadingStoppedTemplet : Templet {
            override val from: BusNodeState = STOPPED
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadingLoadingTemplet : Templet {
            override val from: BusNodeState = LOADING
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadingLoadedTemplet : Templet {
            override val from: BusNodeState = LOADED
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadingBusyTemplet : Templet {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object UnloadedTemplet : Templet {
            override val from: BusNodeState = UNLOADING
            override val unto: BusNodeState = UNLOADED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizingUnloadedTemplet : Templet {
            override val from: BusNodeState = UNLOADED
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizingBusyTemplet : Templet {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizingInitializedTemplet : Templet {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizingInitializingTemplet : Templet {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object FinalizedTemplet : Templet {
            override val from: BusNodeState = FINALIZING
            override val unto: BusNodeState = FINALIZED

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingFinalizedTemplet : Templet {
            override val from: BusNodeState = FINALIZED
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingAttachingTemplet : Templet {
            override val from: BusNodeState = ATTACHING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingAttachedTemplet : Templet {
            override val from: BusNodeState = ATTACHED
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingInitializingTemplet : Templet {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachingFinalizingTemplet : Templet {
            override val from: BusNodeState = FINALIZING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?): BusNodeStateTransition = BasicNodeStateTransition(origin, from, unto, context)
        }

        object DetachedTemplet : Templet {
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
