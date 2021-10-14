/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class BasicMessage<I: Information>(
    content: I,
    override val recipient: Recipient,
    origin: Origin = content.origin,
    priority: Priority = content.priority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = content.validFrom,
    validUpTo: ZonedDateTime = content.validUpTo
) : BasicNotification<I>(content, origin, priority, createdAt, session, validFrom, validUpTo), Message<I>
