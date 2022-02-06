/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.gui.model.FrameProperties

interface FrameDelegator: WindowDelegator {
    override val properties: FrameProperties
    val title: String
}
