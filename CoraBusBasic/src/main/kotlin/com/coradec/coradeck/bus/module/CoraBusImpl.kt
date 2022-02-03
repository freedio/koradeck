/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.module

import com.coradec.coradeck.bus.model.BusApplication
import com.coradec.coradeck.bus.model.BusHub
import com.coradec.coradeck.bus.model.delegation.*
import com.coradec.coradeck.bus.model.impl.*
import com.coradec.coradeck.bus.trouble.NoBusApplicationOnThreadException
import com.coradec.coradeck.core.trouble.InvalidRequestException
import com.coradec.coradeck.dir.model.DirectoryNamespace

class CoraBusImpl : CoraBusAPI {
    override val systemBus: BusHub get() = SystemBus
    override val machineBus: BusHub get() = MachineBus
    override val applicationBus: BusHub get() = ApplicationBus
    private val myApplication = ThreadLocal<BusApplication?>()
    override var application: BusApplication
        get() = myApplication.get() ?: throw NoBusApplicationOnThreadException()
        set(value) {
            if (myApplication.get() != null) throw InvalidRequestException("Trial to override application!")
            myApplication.set(value)
        }

    override fun createNode(delegator: NodeDelegator?): BusNodeDelegate = BusNodeImpl(delegator)
    override fun createHub(delegator: HubDelegator?, namespace: DirectoryNamespace): BusHubDelegate =
        BusHubImpl(delegator, namespace)

    override fun createEngine(delegator: EngineDelegator?): BusEngineDelegate = BusEngineImpl(delegator)
    override fun createMachine(delegator: MachineDelegator?, namespace: DirectoryNamespace): BusMachineDelegate =
        BusMachineImpl(delegator, namespace)
}
