/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusApplication
import com.coradec.coradeck.bus.module.CoraBus

@Suppress("LeakingThis")
abstract class BasicBusApplication(name: String, args: List<String>): BasicBusMachine(), BusApplication {
    private val appName = name
    protected val commandLineArguments = args

    init {
        CoraBus.applicationBus.add(appName, memberView)
        CoraBus.application = this
    }
}
