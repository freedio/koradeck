package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.trouble.IllegalRequestException
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Expiration.Companion.never_expires
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class BasicMessage(
    origin: Origin,
    urgent: Boolean = false,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = never_expires,
    target: Recipient? = null
) : BasicInformation(origin, urgent, created, session, expires), Message {
    override var recipient: Recipient? = target
        set(value) = when (field) {
            null -> field = value
            value -> relax()
            else -> throw IllegalRequestException("Cannot change recipient!")
        }
    override val copy: Message get() = BasicMessage(origin, urgent, createdAt, session, expires, recipient)
    override fun copy(recipient: Recipient) = BasicMessage(origin, urgent, createdAt, session, expires, recipient)
    override fun withRecipient(target: Recipient) =
        if (recipient == null) this.also { recipient = target } else copy(recipient = target)
    override fun withDefaultRecipient(target: Recipient?) =
        if (recipient == null) this.also { recipient = target } else this

    override fun enqueue(target: Recipient) {
        super.enqueue()
        if (recipient != null && recipient != target) throw IllegalRequestException("Recipient cannot be overridden!")
        recipient = target
    }
}
