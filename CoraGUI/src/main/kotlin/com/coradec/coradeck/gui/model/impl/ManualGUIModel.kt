/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.impl

import com.coradec.coradeck.bus.module.CoraBus
import com.coradec.coradeck.gui.model.Frame
import com.coradec.coradeck.gui.model.GUIModel
import java.util.concurrent.ConcurrentHashMap

class ManualGUIModel(name: String) : GUIModel {
    private val myMembers = ConcurrentHashMap<String, Frame>()
    override val members: Map<String, Frame> get() = myMembers

    override fun get(name: String): Frame? = myMembers[name]
    override fun set(name: String, frame: Frame) {
        myMembers[name] = frame
        CoraBus.application.add(name, frame.memberView)
    }
}
