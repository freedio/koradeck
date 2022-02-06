/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.bus.model.delegation.BusNodeDelegate
import com.coradec.coradeck.gui.model.Component
import com.coradec.coradeck.gui.model.ComponentProperties

interface ComponentDelegate: Component, BusNodeDelegate {
    /** The properties. */
    val properties: ComponentProperties
    /** The delegator. */
    override val delegator: ComponentDelegator?
}
