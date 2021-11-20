/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.bus.model.BusNodeState.*
import com.coradec.coradeck.bus.model.impl.BasicNodeStateTransition
import com.coradec.coradeck.bus.trouble.TransitionNotFoundException
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin

interface BusNodeStateTransition : Request {
    val from: BusNodeState
    val unto: BusNodeState
    val context: BusContext?
    val member: MemberView

    interface Templet {
        val from: BusNodeState
        val unto: BusNodeState
        fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition
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

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object AttachingTemplet : Templet {
            override val from: BusNodeState = UNATTACHED
            override val unto: BusNodeState = ATTACHING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object AttachedTemplet : Templet {
            override val from: BusNodeState = ATTACHING
            override val unto: BusNodeState = ATTACHED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object InitializingTemplet : Templet {
            override val from: BusNodeState = ATTACHED
            override val unto: BusNodeState = INITIALIZING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object InitializedTemplet : Templet {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = INITIALIZED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object LoadingTemplet : Templet {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = LOADING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object LoadedTemplet : Templet {
            override val from: BusNodeState = LOADING
            override val unto: BusNodeState = LOADED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object StartingInitializedTemplet : Templet {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = STARTING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object StartingLoadedTemplet : Templet {
            override val from: BusNodeState = LOADED
            override val unto: BusNodeState = STARTING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object StartedTemplet : Templet {
            override val from: BusNodeState = STARTING
            override val unto: BusNodeState = STARTED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object PausingTemplet : Templet {
            override val from: BusNodeState = STARTED
            override val unto: BusNodeState = PAUSING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object PausedTemplet : Templet {
            override val from: BusNodeState = PAUSING
            override val unto: BusNodeState = PAUSED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object ResumingTemplet : Templet {
            override val from: BusNodeState = PAUSED
            override val unto: BusNodeState = RESUMING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object ResumedTemplet : Templet {
            override val from: BusNodeState = RESUMING
            override val unto: BusNodeState = STARTED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object StoppingPausedTemplet : Templet {
            override val from: BusNodeState = PAUSED
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object StoppingStartedTemplet : Templet {
            override val from: BusNodeState = STARTED
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object StoppingBusyTemplet : Templet {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = STOPPING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object StoppedTemplet : Templet {
            override val from: BusNodeState = STOPPING
            override val unto: BusNodeState = STOPPED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object UnloadingStoppedTemplet : Templet {
            override val from: BusNodeState = STOPPED
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object UnloadingLoadingTemplet : Templet {
            override val from: BusNodeState = LOADING
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object UnloadingLoadedTemplet : Templet {
            override val from: BusNodeState = LOADED
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object UnloadingBusyTemplet : Templet {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = UNLOADING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object UnloadedTemplet : Templet {
            override val from: BusNodeState = UNLOADING
            override val unto: BusNodeState = UNLOADED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object FinalizingUnloadedTemplet : Templet {
            override val from: BusNodeState = UNLOADED
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object FinalizingBusyTemplet : Templet {
            override val from: BusNodeState = BUSY
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object FinalizingInitializedTemplet : Templet {
            override val from: BusNodeState = INITIALIZED
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object FinalizingInitializingTemplet : Templet {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = FINALIZING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object FinalizedTemplet : Templet {
            override val from: BusNodeState = FINALIZING
            override val unto: BusNodeState = FINALIZED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object DetachingFinalizedTemplet : Templet {
            override val from: BusNodeState = FINALIZED
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object DetachingAttachingTemplet : Templet {
            override val from: BusNodeState = ATTACHING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object DetachingAttachedTemplet : Templet {
            override val from: BusNodeState = ATTACHED
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object DetachingInitializingTemplet : Templet {
            override val from: BusNodeState = INITIALIZING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object DetachingFinalizingTemplet : Templet {
            override val from: BusNodeState = FINALIZING
            override val unto: BusNodeState = DETACHING

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        object DetachedTemplet : Templet {
            override val from: BusNodeState = DETACHING
            override val unto: BusNodeState = DETACHED

            override fun transition(origin: Origin, context: BusContext?, member: MemberView): BusNodeStateTransition =
                BasicNodeStateTransition(origin, from, unto, member, context)
        }

        @Throws(TransitionNotFoundException::class)
        operator fun invoke(
            origin: Origin,
            initial: BusNodeState,
            terminal: BusNodeState,
            context: BusContext?,
            member: MemberView
        ): BusNodeStateTransition =
            transitions.singleOrNull { it.from == initial && it.unto == terminal }?.transition(origin, context, member)
                ?: throw TransitionNotFoundException(initial, terminal)
    }
}
