/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.delegation

import com.coradec.coradeck.bus.model.BusNode

interface DelegatedBusNode: BusNode {
    /** The delegate to which all operations are delegated. */
    val delegate: BusNodeDelegate
}
