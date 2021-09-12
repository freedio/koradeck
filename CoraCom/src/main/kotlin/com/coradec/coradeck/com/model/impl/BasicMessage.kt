package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.State.NEW
import com.coradec.coradeck.com.trouble.IllegalRequestException
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

open class BasicMessage(
    origin: Origin,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    target: Recipient? = null,
    validFrom: ZonedDateTime = createdAt,
    validUpTo: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
) : BasicInformation(origin, priority, createdAt, session, validFrom, validUpTo), Message {
    override var recipient: Recipient? = target
        set(value) = when (field) {
            null -> field = value
            value -> relax()
            else -> throw IllegalRequestException("Cannot change recipient!")
        }
    override fun withRecipient(target: Recipient) =
        if (recipient == null) this.also { recipient = target } else copy("target" to target)
    override fun withDefaultRecipient(target: Recipient?) = when {
        recipient == null -> this.apply { recipient = target }
        state == NEW -> this
        else -> copy("target" to recipient)
    }

    override fun enqueue(target: Recipient) {
        super.enqueue()
        if (recipient != null && recipient != target) throw IllegalRequestException("Recipient cannot be overridden!")
        recipient = target
    }
}
