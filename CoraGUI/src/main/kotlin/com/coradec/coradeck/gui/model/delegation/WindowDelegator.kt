/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import java.awt.Window

interface WindowDelegator: ContainerDelegator {
    val owner: Window // could also be Frame or JWindow
}
