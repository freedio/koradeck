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
    override val copy: BasicMessage get() = BasicMessage(origin, urgent = urgent)
    override var recipient: Recipient? = target
        set(value) = when (field) {
            null -> field = value
            value -> relax()
            else -> throw IllegalRequestException("Cannot change recipient!")
        }

    override fun enqueue(target: Recipient) {
        super.enqueue()
        if (recipient != null && recipient != target) throw IllegalRequestException("Recipient cannot be overridden!")
        recipient = target
    }

    override fun enqueue() = super.enqueue().also {
        if (recipient == null) throw IllegalArgumentException("Recipient must be present to use this method!")
    }
}
