/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl

import com.coradec.coradeck.gui.model.Section
import com.coradec.coradeck.gui.model.SectionIndex

interface SectionLayout: Layout {
    val indices: Iterable<SectionIndex>

    operator fun get(index: SectionIndex): Section
}
