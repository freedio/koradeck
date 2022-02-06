/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.gui.ctrl.GUI
import kotlin.reflect.KClass

interface GUIType {
    val name: String
    val module: KClass<GUI>

    companion object {
        private val PROP_TYPES = LocalProperty<Map<String, KClass<GUI>>>("Modules")
    }
}
