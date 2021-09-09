package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.trouble.IllegalRequestException
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

open class BasicMessage(
    origin: Origin,
    urgent: Boolean = false,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    target: Recipient? = null,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
) : BasicInformation(origin, urgent, createdAt, session, validFrom, validUpto), Message {
    override var recipient: Recipient? = target
        set(value) = when (field) {
            null -> field = value
            value -> relax()
            else -> throw IllegalRequestException("Cannot change recipient!")
        }
    override val copy: Message get() = copy(recipient)
    override fun copy(recipient: Recipient?) = BasicMessage(origin, urgent, createdAt, session, recipient, validFrom, validUpTo)
    override fun withRecipient(target: Recipient) =
        if (recipient == null) this.also { recipient = target } else copy(target)
    override fun withDefaultRecipient(target: Recipient?) =
        if (recipient == null) this.also { recipient = target } else this

    override fun enqueue(target: Recipient) {
        super.enqueue()
        if (recipient != null && recipient != target) throw IllegalRequestException("Recipient cannot be overridden!")
        recipient = target
    }
}
