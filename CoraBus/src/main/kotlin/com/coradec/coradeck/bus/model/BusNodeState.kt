/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.ctrl.model.State

abstract class BusNodeState : State {
    operator fun compareTo(other: BusNodeState) = rank - other.rank
    operator fun plus(others: Iterable<BusNodeState>) = listOf(this) + others
    operator fun plus(other: BusNodeState) = listOf(this) + other
    override fun toString() = name

    object UNATTACHED : BusNodeState() {
        override val rank = 0
        override val name = "Unattached"
    }

    object ATTACHING : BusNodeState() {
        override val rank = 99
        override val name = "Attaching"
    }

    object ATTACHED : BusNodeState() {
        override val rank = 100
        override val name = "Attached"
    }

    object INITIALIZING : BusNodeState() {
        override val rank = 199
        override val name = "Initializing"
    }

    object INITIALIZED : BusNodeState() {
        override val rank = 200
        override val name = "Initialized"
    }

    object LOADING : BusNodeState() {
        override val rank = 299
        override val name = "Loading"
    }

    object LOADED : BusNodeState() {
        override val rank = 300
        override val name = "Loaded"
    }

    object STARTING : BusNodeState() {
        override val rank = 360
        override val name = "Starting"
    }

    object STARTED : BusNodeState() {
        override val rank = 400
        override val name = "Started"
    }

    object PAUSING : BusNodeState() {
        override val rank = 370
        override val name = "Pausing"
    }

    object PAUSED : BusNodeState() {
        override val rank = 380
        override val name = "Paused"
    }

    object RESUMING : BusNodeState() {
        override val rank = 390
        override val name = "Resuming"
    }

    object STOPPING : BusNodeState() {
        override val rank = 499
        override val name = "Stopping"
    }

    object STOPPED : BusNodeState() {
        override val rank = 500
        override val name = "Stopped"
    }

    object UNLOADING : BusNodeState() {
        override val rank = 599
        override val name = "Unloading"
    }

    object UNLOADED : BusNodeState() {
        override val rank = 600
        override val name = "Unloaded"
    }

    object FINALIZING : BusNodeState() {
        override val rank = 699
        override val name = "Finalizing"
    }

    object FINALIZED : BusNodeState() {
        override val rank = 700
        override val name = "Finalized"
    }

    object DETACHING : BusNodeState() {
        override val rank = 899
        override val name = "Detaching"
    }

    object DETACHED : BusNodeState() {
        override val rank = 900
        override val name = "Detached"
    }

    object READY : BusNodeState() {
        override val rank = 1000
        override val name = "Ready"
    }

    object BUSY : BusNodeState() {
        override val rank = -1
        override val name = "Busy"
    }
}
