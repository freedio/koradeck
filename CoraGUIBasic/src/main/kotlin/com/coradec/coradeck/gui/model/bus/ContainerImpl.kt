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
import com.coradec.coradeck.gui.view.ContainerView
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText

abstract class ContainerImpl(
    override val delegator: ContainerDelegator? = null
) : BusHubImpl(delegator), ContainerDelegate {
    @Suppress("LeakingThis")
    override val properties: ContainerProperties = BasicContainerProperties(this)
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

    override fun createBusHubView(session: Session) = InternalContainerView(session)

    protected inner class InternalContainerView(session: Session) : InternalBusHubView(session), ContainerView {
        override val my = this@ContainerImpl
        override var visible: Boolean
            get() = my.properties.visible
            set(value) { my.properties.visible = value }
        override var enabled: Boolean
            get() = my.properties.enabled
            set(value) { my.properties.enabled = value }
        override var editable: Boolean
            get() = my.properties.editable
            set(value) { my.properties.editable = value }
    }

    companion object {
        val TEXT_MUTABILITY_REQUEST_IGNORED = LocalText("MutabilityRequestIgnored")
    }
}
