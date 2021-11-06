/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusEngineDelegate
import com.coradec.coradeck.bus.model.DelegatedBusEngine
import com.coradec.coradeck.bus.model.EngineDelegator
import com.coradec.coradeck.bus.module.CoraBus

abstract class BasicBusEngine : BasicBusNode(), DelegatedBusEngine {
    override val delegate: BusEngineDelegate = CoraBus.createEngine(InternalEngineDelegator())

    protected open fun onStarting() {}
    protected open fun onStarted() {}
    protected open fun onPausing() {}
    protected open fun onPaused() {}
    protected open fun onResuming() {}
    protected open fun onResumed() {}
    protected open fun onStopping() {}
    protected open fun onStopped() {}

    protected open inner class InternalEngineDelegator : InternalNodeDelegator(), EngineDelegator {
        override fun onStarting() = this@BasicBusEngine.onStarting()
        override fun onStarted() = this@BasicBusEngine.onStarted()
        override fun onPausing() = this@BasicBusEngine.onPausing()
        override fun onPaused() = this@BasicBusEngine.onPaused()
        override fun onResuming() = this@BasicBusEngine.onResuming()
        override fun onResumed() = this@BasicBusEngine.onResumed()
        override fun onStopping() = this@BasicBusEngine.onStopping()
        override fun onStopped() = this@BasicBusEngine.onStopped()
        override fun run() = this@BasicBusEngine.run()
    }
}
