/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

interface DelegatedBusHub: BusHub {
    /** The delegate to which all operations are delegated. */
    val delegate: BusHubDelegate
}
