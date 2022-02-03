/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.delegation.BusMachineDelegate
import com.coradec.coradeck.bus.model.delegation.DelegatedBusMachine
import com.coradec.coradeck.bus.model.delegation.MachineDelegator
import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.module.CoraDir

abstract class BasicBusMachine(namespace: DirectoryNamespace = CoraDir.defaultNamespace) : BasicBusHub(namespace),
    DelegatedBusMachine {
    override val delegate: BusMachineDelegate = CoraBus.createMachine(InternalMachineDelegator())
    protected lateinit var thread: Thread

    protected open fun onStarting() {}
    protected open fun onStarted(): Boolean = true
    protected open fun onPausing() {}
    protected open fun onPaused(): Boolean = true
    protected open fun onResuming() {}
    protected open fun onResumed(): Boolean = true
    protected open fun onStopping() {}
    protected open fun onStopped(): Boolean = true

    protected open inner class InternalMachineDelegator : InternalHubDelegator(), MachineDelegator {
        override var thread: Thread
            get() = this@BasicBusMachine.thread
            set(value) {
                this@BasicBusMachine.thread = value
            }

        override fun onStarting() = this@BasicBusMachine.onStarting()
        override fun onStarted() = this@BasicBusMachine.onStarted()
        override fun onPausing() = this@BasicBusMachine.onPausing()
        override fun onPaused() = this@BasicBusMachine.onPaused()
        override fun onResuming() = this@BasicBusMachine.onResuming()
        override fun onResumed() = this@BasicBusMachine.onResumed()
        override fun onStopping() = this@BasicBusMachine.onStopping()
        override fun onStopped() = this@BasicBusMachine.onStopped()
        override fun run() = this@BasicBusMachine.run()
    }
}
