package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

open class BasicEvent(
    origin: Origin,
    urgent: Boolean = false,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
) : BasicInformation(origin, urgent, createdAt, session, validFrom, validUpto), Event {
    override val copy: BasicEvent get() = BasicEvent(origin, urgent, createdAt, session, validFrom, validUpTo)
}
