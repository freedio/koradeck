/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.gui.model.FrameProperties
import com.coradec.coradeck.text.model.Text

interface FrameDelegator: WindowDelegator {
    override val properties: FrameProperties
    val title: Text
}
