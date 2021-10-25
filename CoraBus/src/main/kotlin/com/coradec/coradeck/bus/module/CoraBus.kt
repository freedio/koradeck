/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.module

import com.coradec.coradeck.bus.model.*
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.module.CoraDir
import com.coradec.coradeck.module.model.CoraModule

object CoraBus : CoraModule<CoraBusAPI>() {

    /** Creates a node implementation with the specified optional delegator. */
    fun createNode(delegator: NodeDelegator? = null): BusNodeDelegate = impl.createNode(delegator)

    /** Creates a hub implementation with the specified namespace and optional delegator. */
    fun createHub(delegator: HubDelegator? = null, namespace: DirectoryNamespace = CoraDir.defaultNamespace): BusHubDelegate =
        impl.createHub(delegator, namespace)

    /** Creates an engine implementation with the specified optional delegator. */
    fun createEngine(delegator: EngineDelegator? = null): BusEngineDelegate =
        impl.createEngine(delegator)

    /** Creates a machine implementation with the specified namespace and optional delegator. */
    fun createMachine(
        delegator: MachineDelegator? = null,
        namespace: DirectoryNamespace = CoraDir.defaultNamespace
    ): BusMachineDelegate =
        impl.createMachine(delegator, namespace)
}
