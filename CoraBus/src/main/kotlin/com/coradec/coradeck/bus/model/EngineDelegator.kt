/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

interface EngineDelegator: NodeDelegator {
    fun onStarting()
    fun onStarted()
    fun onPausing()
    fun onPaused()
    fun onResuming()
    fun onResumed()
    fun onStopping()
    fun onStopped()
}
