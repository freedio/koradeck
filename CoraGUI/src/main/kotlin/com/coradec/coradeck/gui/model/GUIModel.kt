/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import com.coradec.coradeck.gui.model.impl.ManualGUIModel

interface GUIModel {
    val members: Map<String, Frame>

    operator fun get(name: String): Frame?
    operator fun set(name: String, frame: Frame)

    companion object {
        operator fun invoke(name: String): GUIModel = ManualGUIModel(name)
    }
}
