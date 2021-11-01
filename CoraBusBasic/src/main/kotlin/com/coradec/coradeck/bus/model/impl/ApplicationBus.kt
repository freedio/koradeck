/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.dir.module.CoraDir

object ApplicationBus : BasicBusHub(CoraDir.defaultNamespace) {
    private const val APPLICATION_BUS_NAME = "apps"

    init {
        CoraBus.machineBus.add(APPLICATION_BUS_NAME, this)
    }
}
