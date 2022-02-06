/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.bus.model.delegation.BusHubDelegate
import com.coradec.coradeck.gui.ctrl.Layout
import com.coradec.coradeck.gui.model.Container
import com.coradec.coradeck.gui.model.ContainerProperties

interface ContainerDelegate: ComponentDelegate, Container, BusHubDelegate {
    /** The properties. */
    override val properties: ContainerProperties
    /** The container layout. */
    var layout: Layout
    /** The delegator. */
    override val delegator: ContainerDelegator?
}
