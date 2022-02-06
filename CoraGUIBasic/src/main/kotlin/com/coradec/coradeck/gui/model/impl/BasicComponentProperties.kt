/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.impl

import com.coradec.coradeck.gui.com.*
import com.coradec.coradeck.gui.model.Component
import com.coradec.coradeck.gui.model.ComponentProperties
import java.awt.Color

open class BasicComponentProperties(override val owner: Component) : BasicWidgetProperties(owner), ComponentProperties {
    private var visibility: Boolean? = null
    private var ability: Boolean? = null
    private var mutability: Boolean? = null
    private var y: Int? = null
    private var x: Int? = null
    private var h: Int? = null
    private var w: Int? = null
    private var fgc: Color? = null
    private var bgc: Color? = null

    override var visible: Boolean
        get() = visibility ?: owner.accept(VisibilityVoucher(this)).content.value.also { visibility = it }
        set(value) {
            if (value != visibility) owner.accept(SetVisibilityRequest(this, value)).content andThen { visibility = value }
        }
    override var enabled: Boolean
        get() = ability ?: owner.accept(AbilityVoucher(this)).content.value.also { ability = it }
        set(value) {
            if (value != ability) owner.accept(SetAbilityRequest(this, value)).content andThen { ability = value }
        }
    override var editable: Boolean
        get() = mutability ?: owner.accept(MutabilityVoucher(this)).content.value.also { mutability = it }
        set(value) {
            if (value != mutability) owner.accept(SetMutabilityRequest(this, value)).content andThen { mutability = value }
        }
    override var top: Int
        get() = y ?: owner.accept(TopVoucher(this)).content.value.also { y = it }
        set(value) {
            if (value != y) owner.accept(SetTopRequest(this, value)).content andThen { y = value }
        }
    override var left: Int
        get() = x ?: owner.accept(LeftVoucher(this)).content.value.also { x = it }
        set(value) {
            if (value != x) owner.accept(SetLeftRequest(this, value)).content andThen { x = value }
        }
    override var height: Int
        get() = h ?: owner.accept(HeightVoucher(this)).content.value.also { h = it }
        set(value) {
            if (value != h) owner.accept(SetHeightRequest(this, value)).content andThen { h = value }
        }
    override var width: Int
        get() = w ?: owner.accept(WidthVoucher(this)).content.value.also { w = it }
        set(value) {
            if (value != w) owner.accept(SetWidthRequest(this, value)).content andThen { w = value }
        }
    override var foregroundColor: Color?
        get() = fgc ?: owner.accept(ForegroundColorVoucher(this)).content.value
        set(value) {
            if (value != fgc) owner.accept(SetForegroundColorRequest(this, value)).content andThen { fgc = value }
        }
    override var backgroundColor: Color?
        get() = bgc ?: owner.accept(BackgroundColorVoucher(this)).content.value
        set(value) {
            if (value != bgc) owner.accept(SetBackgroundColorRequest(this, value)).content andThen { bgc = value }
        }
}
