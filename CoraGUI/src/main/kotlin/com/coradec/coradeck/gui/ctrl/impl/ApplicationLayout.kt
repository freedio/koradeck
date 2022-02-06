/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl.impl

import com.coradec.coradeck.gui.ctrl.Layout
import com.coradec.coradeck.gui.model.Container
import com.coradec.coradeck.gui.model.Section
import com.coradec.coradeck.gui.model.SectionIndex
import com.coradec.coradeck.gui.model.impl.BasicSection
import java.util.concurrent.ConcurrentHashMap

class ApplicationLayout(private val container: Container) : Layout {
    override val sections = ConcurrentHashMap<SectionIndex, Section>()

    override fun contains(section: SectionIndex): Boolean = section in ApplicationSectionIndex.values()
    override fun get(section: SectionIndex): Section? =
        if (contains(section)) sections.computeIfAbsent(section) { BasicSection(container) } else null

    enum class ApplicationSectionIndex: SectionIndex {
        CONTENT_PLANE, CONTROL_PLANE
    }
}
