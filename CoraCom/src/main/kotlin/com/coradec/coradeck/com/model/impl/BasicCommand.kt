package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Command
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

abstract class BasicCommand(
    origin: Origin,
    urgent: Boolean = false,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires,
    target: Recipient? = null
): BasicRequest(origin, urgent, created, session, expires, target), Command {
    abstract override val copy: BasicCommand
    abstract override fun copy(recipient: Recipient): BasicCommand
}
