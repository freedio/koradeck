/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.NotificationState
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

class StateChangedEvent(
    origin: Origin,
    val message: Notification<*>,
    val previous: NotificationState,
    val current: NotificationState,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime  = validFrom + CoraCom.standardValidity
) : BasicEvent(origin, priority, createdAt, session, validFrom, validUpto)
