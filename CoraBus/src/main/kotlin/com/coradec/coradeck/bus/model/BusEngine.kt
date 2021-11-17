/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import java.util.concurrent.atomic.AtomicInteger

interface BusEngine : BusNode, Runnable {
    companion object {
        val ID_ENGINE = AtomicInteger(0)
    }
}
