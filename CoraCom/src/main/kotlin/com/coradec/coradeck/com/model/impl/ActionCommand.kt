/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

class ActionCommand(
    origin: Origin,
    private val action: () -> Unit,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpTo: ZonedDateTime  = validFrom + CoraCom.standardValidity
): BasicCommand(origin, priority, createdAt, session, validFrom, validUpTo) {
    override fun execute() {
        try {
            action.invoke()
            succeed()
        } catch (e: Exception) {
            fail(e)
        }
    }
}
