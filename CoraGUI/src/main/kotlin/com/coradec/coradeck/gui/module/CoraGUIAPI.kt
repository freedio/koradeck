/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.module

import com.coradec.coradeck.gui.ctrl.GUI
import com.coradec.coradeck.gui.model.GUIType
import com.coradec.coradeck.module.model.CoraModuleAPI

interface CoraGUIAPI: CoraModuleAPI {
    /** Creates a GUI of the specified type. */
    fun createGUI(type: GUIType): GUI
}
