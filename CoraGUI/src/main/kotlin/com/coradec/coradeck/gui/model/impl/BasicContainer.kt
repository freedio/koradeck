/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.impl

import com.coradec.coradeck.bus.model.impl.BasicBusHub
import com.coradec.coradeck.gui.ctrl.Layout
import com.coradec.coradeck.gui.model.ContainerProperties
import com.coradec.coradeck.gui.model.Widget
import com.coradec.coradeck.gui.model.delegation.ContainerDelegate
import com.coradec.coradeck.gui.model.delegation.ContainerDelegator
import com.coradec.coradeck.gui.model.delegation.DelegatedContainer

abstract class BasicContainer : BasicBusHub(), Widget, DelegatedContainer {
    abstract override val delegate: ContainerDelegate

    protected open fun onShown() {}
    protected open fun onHidden() {}
    protected open fun onEnabled() {}
    protected open fun onDisabled() {}
    protected open fun onMutable() {}
    protected open fun onImmutable() {}
    protected abstract val layout: Layout

    protected open inner class InternalContainerDelegator : InternalHubDelegator(), ContainerDelegator {
        override val properties: ContainerProperties get() = delegate.properties
        override val layout: Layout get() = delegate.layout
        override fun onShown() = this@BasicContainer.onShown()
        override fun onHidden() = this@BasicContainer.onHidden()
        override fun onEnabled() = this@BasicContainer.onEnabled()
        override fun onDisabled() = this@BasicContainer.onDisabled()
        override fun onMutable() = this@BasicContainer.onMutable()
        override fun onImmutable() = this@BasicContainer.onImmutable()
    }
}
