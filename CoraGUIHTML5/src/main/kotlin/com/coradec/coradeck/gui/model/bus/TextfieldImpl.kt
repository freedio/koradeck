/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.gui.model.delegation.TextfieldDelegate
import com.coradec.coradeck.gui.model.delegation.TextfieldDelegator
import javax.swing.text.Document

open class TextfieldImpl(override val delegator: TextfieldDelegator? = null) : ComponentImpl(delegator), TextfieldDelegate {
    private val document: Document? get() = delegator?.document
    private val text: String? get() = delegator?.text
    private val columns: Int get() = delegator?.columns ?: 0
}
