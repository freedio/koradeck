/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.impl

import com.coradec.coradeck.bus.model.impl.BasicBusNode
import com.coradec.coradeck.gui.model.ComponentProperties
import com.coradec.coradeck.gui.model.Widget
import com.coradec.coradeck.gui.model.delegation.ComponentDelegate
import com.coradec.coradeck.gui.model.delegation.ComponentDelegator
import com.coradec.coradeck.gui.model.delegation.DelegatedComponent

abstract class BasicComponent : BasicBusNode(), Widget, DelegatedComponent {
    abstract override val delegate: ComponentDelegate

    protected open fun onShown() {}
    protected open fun onHidden() {}
    protected open fun onEnabled() {}
    protected open fun onDisabled() {}
    protected open fun onMutable() {}
    protected open fun onImmutable() {}

    protected open inner class InternalComponentDelegator : InternalNodeDelegator(), ComponentDelegator {
        override val properties: ComponentProperties get() = delegate.properties

        override fun onShown() = this@BasicComponent.onShown()
        override fun onHidden() = this@BasicComponent.onHidden()
        override fun onEnabled() = this@BasicComponent.onEnabled()
        override fun onDisabled() = this@BasicComponent.onDisabled()
        override fun onMutable() = this@BasicComponent.onMutable()
        override fun onImmutable() = this@BasicComponent.onImmutable()
    }
}
