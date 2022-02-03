/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.delegation

import com.coradec.coradeck.bus.model.BusEngine

interface BusEngineDelegate: BusNodeDelegate, BusEngine {
    /** Indicates that the engine has crashed. */
    fun crash()

    /** The delegator. */
    override val delegator: EngineDelegator?
}
