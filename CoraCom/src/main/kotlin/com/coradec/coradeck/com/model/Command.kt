/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.model.impl.ActionCommand
import com.coradec.coradeck.core.model.Origin

interface Command: Request {
    fun execute()

    companion object {
        operator fun invoke(origin: Origin, recipient: Recipient, action: () -> Unit) = ActionCommand(origin, recipient, action)
    }
}
