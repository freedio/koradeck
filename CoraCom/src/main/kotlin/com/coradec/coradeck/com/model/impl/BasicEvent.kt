package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class BasicEvent(
    origin: Origin,
    urgent: Boolean = false,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires
) : BasicInformation(origin, urgent, created, session, expires), Event {
    override val copy: BasicEvent get() = BasicEvent(origin, urgent, createdAt, session, expires)
}
