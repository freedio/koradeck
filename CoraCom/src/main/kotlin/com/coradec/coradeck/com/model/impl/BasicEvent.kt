package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class BasicEvent(
    origin: Origin,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires,
    urgent: Boolean = false
) : BasicInformation(origin, created, session, expires, urgent), Event {
    override val copy: BasicEvent get() = BasicEvent(origin, createdAt, session, expires, urgent)
}
