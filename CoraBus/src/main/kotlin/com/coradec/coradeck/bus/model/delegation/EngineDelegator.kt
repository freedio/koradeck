/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.delegation

interface EngineDelegator: NodeDelegator, Runnable {
    var thread: Thread

    /** Invoked before the engine is starting.  The engine can throw an exception to refuse. */
    fun onStarting()
    /** Invoked when the engine is starting.  `true` if successfully started, `false` if pending. */
    fun onStarted(): Boolean
    /** Invoked before the engine is pausing.  The engine can throw an exception to refuse. */
    fun onPausing()
    /** Invoked when the engine is pausing.  `true` if successfully paused, `false` if pending. */
    fun onPaused(): Boolean
    /** Invoked before the engine is resuming after a pause.  The engine can throw an exception to refuse. */
    fun onResuming()
    /** Invoked when the engine is resuming.  `true` if successfully resumed, `false` if pending. */
    fun onResumed(): Boolean
    /** Invoked before the engine is stopping.  The engine can throw an exception to refuse. */
    fun onStopping()
    /** Invoked when the engine is stopping.  `true` if successfully stopped, `false` if pending. */
    fun onStopped(): Boolean
}
