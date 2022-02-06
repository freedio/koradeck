/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.view

import com.coradec.coradeck.bus.view.BusHubView

interface ContainerView : BusHubView {
    var visible: Boolean
    var enabled: Boolean
    var editable: Boolean
}
