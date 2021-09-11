package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ActionCommand(
    origin: Origin,
    private val action: () -> Unit,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    target: Recipient? = null,
    validFrom: ZonedDateTime = createdAt,
    validUpTo: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
): BasicCommand(origin, priority, createdAt, session, target, validFrom, validUpTo) {
    override val copy: ActionCommand get() = ActionCommand(origin, action, priority, createdAt,session, recipient, validFrom, validUpTo)
    override fun copy(recipient: Recipient?): ActionCommand =
        ActionCommand(origin, action, priority, createdAt,session, recipient, validFrom, validUpTo)

    override fun execute() {
        try {
            action.invoke()
            succeed()
        } catch (e: Exception) {
            fail(e)
        }
    }
}
