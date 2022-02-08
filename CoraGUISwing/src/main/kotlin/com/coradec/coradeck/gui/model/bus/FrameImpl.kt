/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.gui.com.SetVisibilityRequest
import com.coradec.coradeck.gui.model.delegation.FrameDelegate
import com.coradec.coradeck.gui.model.delegation.FrameDelegator
import com.coradec.coradeck.gui.model.impl.BasicFrameProperties
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.Text
import javax.swing.JFrame

class FrameImpl(override val delegator: FrameDelegator? = null) : WindowImpl(delegator), FrameDelegate {
    override val properties = BasicFrameProperties(this)
    override val title: Text = delegator?.title ?: TEXT_DEFAULT_FRAME_TITLE
    override val peer: JFrame = JFrame(title.content)
    private var packed: Boolean = false

    override fun setVisibility(request: SetVisibilityRequest) {
        if (request.visible && !packed) {
            peer.pack()
            packed = true
        }
        super.setVisibility(request)
    }

    companion object {
        val TEXT_DEFAULT_FRAME_TITLE = LocalText("DefaultFrameTitle")
    }
}
