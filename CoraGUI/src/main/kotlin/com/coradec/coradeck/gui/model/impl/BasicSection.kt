/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.impl

import com.coradec.coradeck.gui.ctrl.Layout
import com.coradec.coradeck.gui.model.Component
import com.coradec.coradeck.gui.model.Section
import com.coradec.coradeck.gui.model.SectionIndex

class BasicSection(private val index: SectionIndex) : Section {
    private val components = mutableListOf<Component>()
    override val layout: Layout get() = index.defaultLayout

    override fun add(component: Component) {
        components += component
    }
}
