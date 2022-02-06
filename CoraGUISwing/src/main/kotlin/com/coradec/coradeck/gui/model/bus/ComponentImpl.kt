/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.bus.model.impl.BusNodeImpl
import com.coradec.coradeck.gui.com.*
import com.coradec.coradeck.gui.model.ComponentProperties
import com.coradec.coradeck.gui.model.delegation.ComponentDelegate
import com.coradec.coradeck.gui.model.delegation.ComponentDelegator
import com.coradec.coradeck.gui.model.impl.BasicComponentProperties
import com.coradec.coradeck.gui.view.ComponentView
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.awt.Component
import java.awt.Rectangle

abstract class ComponentImpl(
    override val delegator: ComponentDelegator? = null
) : BusNodeImpl(delegator), ComponentDelegate {
    protected abstract val peer: Component
    @Suppress("LeakingThis")
    override val properties: ComponentProperties = BasicComponentProperties(this)
    override val componentView: ComponentView get() = componentView(Session.current)
    var visible: Boolean
        get() = properties.visible
        set(value) { properties.visible = value }
    var enabled: Boolean
        get() = properties.enabled
        set(value) { properties.enabled = value }
    var editable: Boolean
        get() = properties.editable
        set(value) { properties.editable = value }

    private fun componentView(session: Session) =
        session.view[this, ComponentView::class]
            ?: InternalComponentView(session).also { session.view[this, ComponentView::class] = it }

    override fun onInitialized(): Boolean {
        route(VisibilityVoucher::class, ::checkVisibility)
        route(AbilityVoucher::class, ::checkAbility)
        route(MutabilityVoucher::class, ::checkMutability)
        route(SetVisibilityRequest::class, ::setVisibility)
        route(SetAbilityRequest::class, ::setAbility)
        route(SetMutabilityRequest::class, ::setMutability)
        if (delegator != null) {
            initializePeer()
        }
        return super.onInitialized()
    }

    protected open fun initializePeer() {
        peer.foreground = properties.foregroundColor
        peer.background = properties.backgroundColor
        peer.bounds = Rectangle(properties.left, properties.top, properties.width, properties.height)
    }

    private fun checkVisibility(voucher: VisibilityVoucher) {
        voucher.value = peer.isVisible
        voucher.succeed()
    }

    private fun checkAbility(voucher: AbilityVoucher) {
        voucher.value = peer.isEnabled
        voucher.succeed()
    }

    protected fun checkMutability(voucher: MutabilityVoucher) {
        voucher.value = false
        voucher.succeed()
    }

    private fun setVisibility(request: SetVisibilityRequest) {
        peer.isVisible = request.visible
        request.succeed()
    }

    private fun setAbility(request: SetAbilityRequest) {
        peer.isEnabled = request.enabled
    }

    protected fun setMutability(request: SetMutabilityRequest) {
        warn(TEXT_MUTABILITY_REQUEST_IGNORED)
        request.succeed()
    }

    protected inner class InternalComponentView(override val session: Session) : ComponentView {
        override var visible: Boolean
            get() = this@ComponentImpl.properties.visible
            set(value) { this@ComponentImpl.properties.visible = value }
        override var enabled: Boolean
            get() = this@ComponentImpl.properties.enabled
            set(value) { this@ComponentImpl.properties.enabled = value }
        override var editable: Boolean
            get() = this@ComponentImpl.properties.editable
            set(value) { this@ComponentImpl.properties.editable = value }
    }

    companion object {
        val TEXT_MUTABILITY_REQUEST_IGNORED = LocalText("MutabilityRequestIgnored")
    }
}
