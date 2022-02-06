/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.module

import com.coradec.coradeck.gui.model.delegation.*
import com.coradec.coradeck.module.model.CoraModule

class CoraGUIModule : CoraModule<CoraGUIModuleAPI>() {
    /** Creates a window implementation with the specified optional delegator. */
    fun createWindow(delegator: WindowDelegator?): WindowDelegate = impl.createWindow(delegator)
    /** Creates a frame implementation with the specified optional delegator. */
    fun createFrame(delegator: FrameDelegator?): FrameDelegate = impl.createFrame(delegator)
    /** Creates a button implementation with the specified optional delegator. */
    fun createButton(delegator: ButtonDelegator?): ButtonDelegate = impl.createButton(delegator)
    /** Creates a textfield implementation with the specified optional delegator. */
    fun createTextfield(delegator: TextfieldDelegator?): TextfieldDelegate = impl.createTextfield(delegator)
    /** Creates a label implementation with the specified optional delegator. */
    fun createLabel(delegator: LabelDelegator?): LabelDelegate = impl.createLabel(delegator)
}
