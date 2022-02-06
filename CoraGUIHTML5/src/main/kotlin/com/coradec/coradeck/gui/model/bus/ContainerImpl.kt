/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.bus.model.impl.BusHubImpl
import com.coradec.coradeck.gui.com.*
import com.coradec.coradeck.gui.ctrl.Layout
import com.coradec.coradeck.gui.ctrl.impl.NoLayout
import com.coradec.coradeck.gui.model.ContainerProperties
import com.coradec.coradeck.gui.model.Section
import com.coradec.coradeck.gui.model.SectionIndex
import com.coradec.coradeck.gui.model.delegation.ContainerDelegate
import com.coradec.coradeck.gui.model.delegation.ContainerDelegator
import com.coradec.coradeck.gui.model.impl.BasicContainerProperties
import com.coradec.coradeck.gui.view.ComponentView
import com.coradec.coradeck.gui.view.ContainerView
import com.coradec.coradeck.session.model.Session

abstract class ContainerImpl(
    override val delegator: ContainerDelegator? = null
) : BusHubImpl(delegator), ContainerDelegate {
    @Suppress("LeakingThis")
    override val properties: ContainerProperties = BasicContainerProperties(this)
    override val componentView: ComponentView get() = componentView(Session.current)
    private val container: ContainerView get() = containerView(Session.current)
    private var myLayout: Layout = NoLayout()
    override var layout: Layout
        get() = myLayout
        set(value) {
            myLayout = value
        }
    val sections: Collection<SectionIndex> get() = myLayout.sections.keys
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
        if (delegator != null) initializePeer()
        return super.onInitialized()
    }

    protected fun initializePeer() {

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

    operator fun get(section: SectionIndex): Section = layout[section]
        ?: throw IllegalArgumentException("Section ‹$section› not found")

    private fun componentView(session: Session) =
        session.view[this, ComponentView::class]
            ?: InternalComponentView(session).also { session.view[this, ComponentView::class] = it }

    protected open inner class InternalComponentView(override val session: Session) : ComponentView {
        override var visible: Boolean
            get() = this@ContainerImpl.properties.visible
            set(value) { this@ContainerImpl.properties.visible = value }
        override var enabled: Boolean
            get() = this@ContainerImpl.properties.enabled
            set(value) { this@ContainerImpl.properties.enabled = value }
        override var editable: Boolean
            get() = this@ContainerImpl.properties.editable
            set(value) { this@ContainerImpl.properties.editable = value }
    }

    private fun containerView(session: Session) = session.view[this, ContainerView::class]
        ?: InternalContainerView(session).also { session.view[this, ContainerView::class] = it }

    protected open inner class InternalContainerView(session: Session): InternalComponentView(session), ContainerView {

    }
}
