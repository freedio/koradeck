/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.trouble

import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.gui.ctrl.Layout
import kotlin.reflect.KClass

class LayoutNotInstantiableException(val layoutClass: KClass<out Layout>) : BasicGUIException(explanation) {
    companion object {
        private val explanation =
            "An instantiable layout must be either a concrete classand have a no-arg primary constructor, or be an abstract " +
                    "class or interface with a companion object that has an operator fun invoke without parameters that returns " +
                    "a ${Layout::class.classname}."
    }
}
