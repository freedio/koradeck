/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.module

import com.coradec.coradeck.gui.model.bus.*
import com.coradec.coradeck.gui.model.delegation.*

class CoraGUIModuleImpl : CoraGUIModuleAPI {
    override fun createWindow(delegator: WindowDelegator?): WindowDelegate = WindowImpl(delegator)
    override fun createFrame(delegator: FrameDelegator?): FrameDelegate = FrameImpl(delegator)
    override fun createButton(delegator: ButtonDelegator?): ButtonDelegate = ButtonImpl(delegator)
    override fun createTextfield(delegator: TextfieldDelegator?): TextfieldDelegate = TextfieldImpl(delegator)
    override fun createLabel(delegator: LabelDelegator?): LabelDelegate = LabelImpl(delegator)
}
