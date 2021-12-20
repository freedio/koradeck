/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class BasicEvent(
    origin: Origin,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = validFrom + CoraCom.standardValidity
) : BasicInformation(origin, priority, createdAt, session, validFrom, validUpto), Event
