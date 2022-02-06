/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.gui.view.ComponentView

interface Component: BusNode, Widget {
    val componentView: ComponentView
}
