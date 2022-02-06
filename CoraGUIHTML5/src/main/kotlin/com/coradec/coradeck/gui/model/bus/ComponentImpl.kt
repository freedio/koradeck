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
import com.coradec.coradeck.gui.view.ContainerView
import com.coradec.coradeck.session.model.Session

abstract class ComponentImpl(
    override val delegator: ComponentDelegator? = null
) : BusNodeImpl(delegator), ComponentDelegate {
    @Suppress("LeakingThis")
    override val properties: ComponentProperties = BasicComponentProperties(this)
    override val componentView: ComponentView get() = componentView(Session.current)
    private val container: ContainerView get() = containerView(Session.current)
    private var visibility: Boolean? = null
    private var ability: Boolean? = null
    private var mutability: Boolean? = null
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
        return super.onInitialized()
    }

    private fun checkVisibility(voucher: VisibilityVoucher) {
        voucher.value = visibility ?: container.visible
        voucher.succeed()
    }

    private fun checkAbility(voucher: AbilityVoucher) {
        voucher.value = ability ?: container.enabled
        voucher.succeed()
    }

    protected fun checkMutability(voucher: MutabilityVoucher) {
        voucher.value = mutability ?: container.editable
        voucher.succeed()
    }

    private fun setVisibility(request: SetVisibilityRequest) {
        visibility = request.visible
        request.succeed()
    }

    private fun setAbility(request: SetAbilityRequest) {
        ability = request.enabled
    }

    protected fun setMutability(request: SetMutabilityRequest) {
        mutability = request.editable
        request.succeed()
    }

    private fun componentView(session: Session) =
        session.view[this, ComponentView::class]
            ?: InternalComponentView(session).also { session.view[this, ComponentView::class] = it }

    protected open inner class InternalComponentView(override val session: Session) : ComponentView {
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

    private fun containerView(session: Session) = session.view[this, ContainerView::class]
            ?: InternalContainerView(session).also { session.view[this, ContainerView::class] = it }

    protected open inner class InternalContainerView(session: Session): InternalComponentView(session), ContainerView {

    }
}
