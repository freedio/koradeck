/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl

import com.coradec.coradeck.gui.ctrl.impl.BasicVerticalLayout
import com.coradec.coradeck.gui.model.Container

interface VerticalLayout: Layout {
    companion object {
        operator fun invoke(container: Container): VerticalLayout = BasicVerticalLayout(container)
    }
}
