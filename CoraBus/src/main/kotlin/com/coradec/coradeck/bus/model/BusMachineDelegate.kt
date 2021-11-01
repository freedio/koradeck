/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

interface BusMachineDelegate: BusHubDelegate, BusMachine {
    /** The delegator. */
    override val delegator: MachineDelegator?
}
