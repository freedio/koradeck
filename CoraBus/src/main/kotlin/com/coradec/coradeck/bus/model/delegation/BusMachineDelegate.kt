/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.delegation

import com.coradec.coradeck.bus.model.BusMachine

interface BusMachineDelegate: BusHubDelegate, BusMachine {
    /** Indicates that the machine has crashed. */
    fun crash()

    /** The delegator. */
    override val delegator: MachineDelegator?
}
