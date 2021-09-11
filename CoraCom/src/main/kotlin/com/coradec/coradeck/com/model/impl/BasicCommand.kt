package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Command
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

abstract class BasicCommand(
    origin: Origin,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    target: Recipient? = null,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
): BasicRequest(origin, priority, createdAt, session, target, validFrom, validUpto), Command {
    abstract override fun copy(recipient: Recipient?): BasicCommand
}
