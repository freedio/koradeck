/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class DummyRequest(
    override val origin: Origin,
    override val priority: Priority = Priority.defaultPriority,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val session: Session = Session.current,
    override val validFrom: ZonedDateTime = createdAt,
    override val validUpTo: ZonedDateTime  = validFrom + CoraCom.standardValidity
) : BasicRequest(origin, priority, createdAt, session, validFrom, validUpTo)
