/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.module

import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.module.CoraDir.defaultNamespace
import com.coradec.coradeck.dir.module.CoraDir.rootNamespace
import com.coradec.coradeck.module.model.CoraModuleAPI

interface CoraBusAPI : CoraModuleAPI {
    /** The system bus. */
    val systemBus: BusHub
    /** The machine bus. */
    val machineBus: BusHub
    /** The application bus. */
    val applicationBus: BusHub

    /** Creates a node implementation with the specified optional delegator. */
    fun createNode(delegator: NodeDelegator? = null): BusNodeDelegate
    /** Creates a hub implementation with the specified namespace and optional delegator. */
    fun createHub(delegator: HubDelegator? = null, namespace: DirectoryNamespace = rootNamespace): BusHubDelegate
    /** Creates an engine implementation with the specified optional delegator. */
    fun createEngine(delegator: EngineDelegator? = null): BusEngineDelegate = CoraBus.impl.createEngine(delegator)
    /** Creates a machine implementation with the specified namespace and optional delegator. */
    fun createMachine(delegator: MachineDelegator? = null, namespace: DirectoryNamespace = defaultNamespace): BusMachineDelegate =
        CoraBus.impl.createMachine(delegator, namespace)
}
