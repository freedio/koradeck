/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.module

import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.bus.model.impl.*
import com.coradec.coradeck.dir.model.DirectoryNamespace

class CoraBusImpl : CoraBusAPI {
    override val systemBus: BusHub = SystemBus
    override fun createNode(delegator: NodeDelegator?): BusNodeDelegate = BusNodeImpl(delegator)
    override fun createHub(delegator: HubDelegator?, namespace: DirectoryNamespace): BusHubDelegate =
        BusHubImpl(delegator, namespace)
    override fun createEngine(delegator: EngineDelegator?): BusEngineDelegate = BusEngineImpl(delegator)
    override fun createMachine(delegator: MachineDelegator?, namespace: DirectoryNamespace): BusMachineDelegate =
        BusMachineImpl(delegator, namespace)
}
