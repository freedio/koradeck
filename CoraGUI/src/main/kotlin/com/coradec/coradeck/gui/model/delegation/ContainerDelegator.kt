/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.bus.model.delegation.HubDelegator
import com.coradec.coradeck.gui.ctrl.Layout
import com.coradec.coradeck.gui.model.ContainerProperties

interface ContainerDelegator: HubDelegator, ComponentDelegator {
    override val properties: ContainerProperties
    val layout: Layout
}
