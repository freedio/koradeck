/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl

import com.coradec.coradeck.gui.ctrl.impl.BasicHorizontalLayout
import com.coradec.coradeck.gui.model.Container

interface HorizontalLayout: Layout {
    companion object {
        operator fun invoke(container: Container): HorizontalLayout = BasicHorizontalLayout(container)
    }
}
