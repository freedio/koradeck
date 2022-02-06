/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.gui.model.HorizontalAlignment
import com.coradec.coradeck.gui.model.LabelProperties
import javax.swing.Icon

interface LabelDelegator: ComponentDelegator {
    override val properties: LabelProperties
    val labelAlignment: HorizontalAlignment
    val labelIcon: Icon?
    val labelText: String?
}
