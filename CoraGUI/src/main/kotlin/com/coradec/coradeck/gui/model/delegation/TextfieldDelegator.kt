/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import javax.swing.text.Document

interface TextfieldDelegator: ComponentDelegator {
    val columns: Int?
    val text: String?
    val document: Document?
}
