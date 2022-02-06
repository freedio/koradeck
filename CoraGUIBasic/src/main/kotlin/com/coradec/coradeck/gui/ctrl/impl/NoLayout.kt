/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl.impl

import com.coradec.coradeck.gui.ctrl.Layout
import com.coradec.coradeck.gui.model.Section
import com.coradec.coradeck.gui.model.SectionIndex

class NoLayout: Layout {
    override val sections: Map<SectionIndex, Section> = emptyMap()
    override fun contains(section: SectionIndex): Boolean= false
    override fun get(section: SectionIndex): Section? = null
}
