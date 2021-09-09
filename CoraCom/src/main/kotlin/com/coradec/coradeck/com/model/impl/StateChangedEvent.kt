package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class StateChangedEvent(
    origin: Origin,
    val source: Information,
    val previous: State,
    val current: State,
    urgent: Boolean = false,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
) : BasicEvent(origin, urgent, createdAt, session, validFrom, validUpto) {
    override val copy: StateChangedEvent get() =
        StateChangedEvent(origin, source, previous, current, urgent, createdAt, session, validUpTo)
}
