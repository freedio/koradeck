/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

interface BusNodeDelegate: BusNode {
    /** The delegator. */
    val delegator: NodeDelegator?
}
