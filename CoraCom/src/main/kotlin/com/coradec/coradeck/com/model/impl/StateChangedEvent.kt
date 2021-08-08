package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

class StateChangedEvent(
    origin: Origin,
    val source: Information,
    val previous: State,
    val current: State,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires,
    urgent: Boolean = false
) : BasicEvent(origin, created, session, expires, urgent) {
    override val copy: StateChangedEvent get() =
        StateChangedEvent(origin, source, previous, current, createdAt, session, expires, urgent)
}
