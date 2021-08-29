package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

class ActionCommand(
    origin: Origin,
    private val action: () -> Unit,
    urgent: Boolean = false,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires,
    target: Recipient? = null
): BasicCommand(origin, urgent, created, session, expires, target) {
    override val copy: ActionCommand get() = ActionCommand(origin, action, urgent, createdAt,session, expires, recipient)
    override fun copy(recipient: Recipient): ActionCommand =
        ActionCommand(origin, action, urgent, createdAt,session, expires, recipient)

    override fun execute() {
        try {
            action.invoke()
            succeed()
        } catch (e: Exception) {
            fail(e)
        }
    }
}
