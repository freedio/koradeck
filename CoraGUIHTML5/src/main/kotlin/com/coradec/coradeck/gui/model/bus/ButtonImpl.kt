/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.core.util.asOrigin
import com.coradec.coradeck.gui.com.ButtonActionEvent
import com.coradec.coradeck.gui.model.delegation.ButtonDelegate
import com.coradec.coradeck.gui.model.delegation.ButtonDelegator
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

open class ButtonImpl(override val delegator: ButtonDelegator? = null) : ComponentImpl(delegator), ButtonDelegate {
    private val action = object: AbstractAction() {
        override fun actionPerformed(event: ActionEvent) {
            accept(ButtonActionEvent(event.source.asOrigin, event.modifiers, event.actionCommand))
        }
    }
}
