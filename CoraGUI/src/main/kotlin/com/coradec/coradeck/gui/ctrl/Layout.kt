/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl

import com.coradec.coradeck.gui.model.Section
import com.coradec.coradeck.gui.model.SectionIndex

interface Layout {
    val sections: Map<SectionIndex, Section>

    operator fun contains(section: SectionIndex): Boolean
    operator fun get(section: SectionIndex): Section?
}
