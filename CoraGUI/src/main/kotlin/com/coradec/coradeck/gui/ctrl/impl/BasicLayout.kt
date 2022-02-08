/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl.impl

import com.coradec.coradeck.gui.ctrl.Layout
import java.awt.Component
import java.awt.Container
import java.util.concurrent.ConcurrentHashMap

abstract class BasicLayout: Layout {
    private val components = ConcurrentHashMap<Component, Any?>()

    override fun addLayoutComponent(comp: Component, constraints: Any?) {
        components[comp] = constraints
    }

    override fun addLayoutComponent(name: String, comp: Component) {
        components[comp] = name
    }

    override fun removeLayoutComponent(comp: Component) {
        components.remove(comp)
    }

    override fun invalidateLayout(target: Container) {
        components.clear()
    }

}
