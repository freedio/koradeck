/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.module

import com.coradec.coradeck.gui.ctrl.BasicGUI
import com.coradec.coradeck.gui.ctrl.GUI
import com.coradec.coradeck.gui.model.GUIType

class CoraGUIImpl : CoraGUIAPI {
    /** Creates a GUI of the specified type. */
    override fun createGUI(type: GUIType): GUI = BasicGUI(type)
}
