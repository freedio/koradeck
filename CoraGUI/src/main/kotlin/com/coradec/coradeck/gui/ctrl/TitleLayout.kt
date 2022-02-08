/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl

import com.coradec.coradeck.gui.ctrl.impl.BasicTitleLayout
import com.coradec.coradeck.gui.model.Component

interface TitleLayout: Layout {
    companion object {
        operator fun invoke(component: Component): TitleLayout = BasicTitleLayout(component)
    }
}
