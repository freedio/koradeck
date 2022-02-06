/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.module

import com.coradec.coradeck.gui.model.delegation.*
import com.coradec.coradeck.module.model.CoraModuleAPI

interface CoraGUIAPI: CoraModuleAPI {
    /** Creates a window implementation with the specified optional delegator. */
    fun createWindow(delegator: WindowDelegator? = null): WindowDelegate
    /** Creates a frame implementation with the specified optional delegator. */
    fun createFrame(delegator: FrameDelegator? = null): FrameDelegate
    /** Creates a button implementation with the specified optional delegator. */
    fun createButton(delegator: ButtonDelegator? = null): ButtonDelegate
    /** Creates a textfield implementation with the specified optional delegator. */
    fun createTextfield(delegator: TextfieldDelegator? = null): TextfieldDelegate
    /** Creates a label implementation with the specified optional delegator. */
    fun createLabel(delegator: LabelDelegator? = null): LabelDelegate
}
