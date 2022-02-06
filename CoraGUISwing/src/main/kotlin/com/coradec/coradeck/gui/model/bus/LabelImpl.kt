/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.gui.model.delegation.LabelDelegate
import com.coradec.coradeck.gui.model.delegation.LabelDelegator
import com.coradec.coradeck.gui.model.impl.BasicLabelProperties
import java.awt.Component
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.SwingConstants

open class LabelImpl(
    override val delegator: LabelDelegator? = null,
    private val labelText: String? = "Label",
    private val labelIcon: Icon? = null
) : ComponentImpl(delegator), LabelDelegate {
    @Suppress("LeakingThis")
    override val properties = BasicLabelProperties(this)
    private val text: String? get() = delegator?.labelText ?: labelText
    private val icon: Icon? get() = delegator?.labelIcon ?: labelIcon
    private val horizontalAlignment: Int get() = delegator?.labelAlignment?.swingIndex ?: SwingConstants.LEADING
    override val peer: Component = JLabel(text, icon, horizontalAlignment)
}
