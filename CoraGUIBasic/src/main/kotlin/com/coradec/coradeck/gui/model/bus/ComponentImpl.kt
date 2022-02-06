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
import com.coradec.coradeck.gui.view.ContainerView
import com.coradec.coradeck.text.model.LocalText
import java.awt.Rectangle

abstract class ComponentImpl(
    override val delegator: ComponentDelegator? = null
) : BusNodeImpl(delegator), ComponentDelegate {
    @Suppress("LeakingThis")
    override val properties: ComponentProperties = BasicComponentProperties(this)
    private val containerView: ContainerView? get() = context?.hub.let { if (it is ContainerView) it else null }
    var visible: Boolean
        get() = properties.visible
        set(value) { properties.visible = value }
    var enabled: Boolean
        get() = properties.enabled
        set(value) { properties.enabled = value }
    var editable: Boolean
        get() = properties.editable
        set(value) { properties.editable = value }

    override fun onInitialized(): Boolean {
        route(VisibilityVoucher::class, ::checkVisibility)
        route(AbilityVoucher::class, ::checkAbility)
        route(MutabilityVoucher::class, ::checkMutability)
        route(SetVisibilityRequest::class, ::setVisibility)
        route(SetAbilityRequest::class, ::setAbility)
        route(SetMutabilityRequest::class, ::setMutability)
        if (delegator != null) initializePeer()
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

    companion object {
        val TEXT_MUTABILITY_REQUEST_IGNORED = LocalText("MutabilityRequestIgnored")
    }
}
