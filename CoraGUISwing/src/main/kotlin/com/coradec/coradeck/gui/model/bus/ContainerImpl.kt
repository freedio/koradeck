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
import com.coradec.coradeck.text.model.LocalText
import java.awt.Container

abstract class ContainerImpl(
    override val delegator: ContainerDelegator? = null
) : BusHubImpl(delegator), ContainerDelegate {
    protected abstract val peer: Container
    @Suppress("LeakingThis")
    override val properties: ContainerProperties = BasicContainerProperties(this)
    override val componentView: ComponentView get() = componentView(Session.current)
    private val containerView: ContainerView? get() = context?.hub.let { if (it is ContainerView) it else null }
    private var myLayout: Layout = NoLayout()
    override var layout: Layout
        get() = myLayout
        set(value) {
            myLayout = value
        }
    val sections: Collection<SectionIndex> get() = myLayout.sections.keys
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
            ?: InternalContainerView(session).also { session.view[this, ComponentView::class] = it }

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

    protected fun initializePeer() {

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

    operator fun get(section: SectionIndex): Section = layout[section]
        ?: throw IllegalArgumentException("Section ‹$section› not found")

    protected inner class InternalContainerView(override val session: Session) : ContainerView {
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

    companion object {
        val TEXT_MUTABILITY_REQUEST_IGNORED = LocalText("MutabilityRequestIgnored")
    }
}
