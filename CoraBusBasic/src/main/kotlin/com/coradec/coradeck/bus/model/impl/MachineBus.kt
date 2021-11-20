/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.dir.module.CoraDir
import java.net.InetAddress

object MachineBus : BasicBusHub(CoraDir.defaultNamespace) {
    private val MACHINE_BUS_NAME = InetAddress.getLocalHost().hostName

    init {
        CoraBus.systemBus.add(MACHINE_BUS_NAME, memberView)
    }
}
