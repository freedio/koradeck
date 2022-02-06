/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.bus.model.delegation.NodeDelegator
import com.coradec.coradeck.gui.model.ComponentProperties

interface ComponentDelegator: NodeDelegator {
    /** The properties of the component. */
    val properties: ComponentProperties

    fun onShown()
    fun onHidden()
    fun onEnabled()
    fun onDisabled()
    fun onMutable()
    fun onImmutable()
}
