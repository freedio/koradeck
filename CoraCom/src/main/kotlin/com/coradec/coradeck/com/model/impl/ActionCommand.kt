package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Command
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin

class ActionCommand(origin: Origin, recipient: Recipient, private val action: () -> Unit): BasicRequest(origin, recipient), Command {
    override fun execute() {
        try {
            action.invoke()
            succeed()
        } catch (e: Exception) {
            fail(e)
        }
    }
}
