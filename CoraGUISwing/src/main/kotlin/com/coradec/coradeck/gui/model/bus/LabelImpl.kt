/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.gui.model.delegation.LabelDelegate
import com.coradec.coradeck.gui.model.delegation.LabelDelegator
import com.coradec.coradeck.gui.model.impl.BasicLabelProperties
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.Text
import java.awt.Component
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.SwingConstants.LEADING

open class LabelImpl(
    override val delegator: LabelDelegator? = null,
    override val labelText: Text = TEXT_DEFAULT_LABEL_TEXT,
    private val labelIcon: Icon? = null
) : ComponentImpl(delegator), LabelDelegate {
    @Suppress("LeakingThis")
    override val properties = BasicLabelProperties(this)
    private val text: Text get() = delegator?.labelText ?: labelText
    private val icon: Icon? get() = delegator?.labelIcon ?: labelIcon
    private val horizontalAlignment: Int get() = delegator?.labelAlignment?.swingIndex ?: LEADING
    override val peer: Component = JLabel(text.content, icon, horizontalAlignment)

    companion object {
        val TEXT_DEFAULT_LABEL_TEXT = LocalText("DefaultLabelText")
    }
}
