/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.gui.model.delegation.FrameDelegate
import com.coradec.coradeck.gui.model.delegation.FrameDelegator
import com.coradec.coradeck.gui.model.impl.BasicFrameProperties
import javax.swing.JFrame

class FrameImpl(override val delegator: FrameDelegator? = null) : WindowImpl(delegator), FrameDelegate {
    override val properties = BasicFrameProperties(this)
    private val title = delegator?.title ?: "Frame"
    override val peer: JFrame = JFrame(title)
}
