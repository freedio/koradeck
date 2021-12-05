/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.core.model.Origin

class MacroCommand(origin: Origin, private val function: () -> Unit) : BasicCommand(origin, ) {
    override fun execute() {
        try {
            function.invoke()
            succeed()
        } catch (e: Exception) {
            error(e)
            fail(e)
        }
    }
}
