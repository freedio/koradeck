package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Expiration.Companion.never_expires
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class BasicMessage(
        origin: Origin,
        override val recipient: Recipient,
        created: ZonedDateTime = ZonedDateTime.now(),
        session: Session = Session.current,
        expires: Expiration = never_expires
) : BasicInformation(origin, created, session, expires), Message
