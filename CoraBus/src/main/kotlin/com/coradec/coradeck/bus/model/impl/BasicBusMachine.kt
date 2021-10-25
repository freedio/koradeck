/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusMachineDelegate
import com.coradec.coradeck.bus.model.DelegatedBusMachine
import com.coradec.coradeck.bus.model.MachineDelegator
import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.dir.model.DirectoryNamespace

open class BasicBusMachine(namespace: DirectoryNamespace) : BasicBusHub(namespace), DelegatedBusMachine {
    override val delegate: BusMachineDelegate = CoraBus.createMachine(InternalMachineDelegator())

    protected open fun onStarting() {}
    protected open fun onStarted() {}
    protected open fun onPausing() {}
    protected open fun onPaused() {}
    protected open fun onResuming() {}
    protected open fun onResumed() {}
    protected open fun onStopping() {}
    protected open fun onStopped() {}

    protected open inner class InternalMachineDelegator : InternalHubDelegator(), MachineDelegator {
        override fun onStarting() = this@BasicBusMachine.onStarting()
        override fun onStarted() = this@BasicBusMachine.onStarted()
        override fun onPausing() = this@BasicBusMachine.onPausing()
        override fun onPaused() = this@BasicBusMachine.onPaused()
        override fun onResuming() = this@BasicBusMachine.onResuming()
        override fun onResumed() = this@BasicBusMachine.onResumed()
        override fun onStopping() = this@BasicBusMachine.onStopping()
        override fun onStopped() = this@BasicBusMachine.onStopped()
    }
}
