package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Command
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

abstract class BasicCommand(
    origin: Origin,
    recipient: Recipient,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires,
    urgent: Boolean = false
): BasicRequest(origin, recipient, created, session, expires, urgent), Command {
    abstract override val copy: BasicCommand
}
