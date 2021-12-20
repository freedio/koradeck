/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.com

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.com.model.impl.BasicEvent
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

class NodeStateChangedEvent(
    origin: Origin,
    val node: BusNode,
    val previous: BusNodeState,
    val current: BusNodeState,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime  = validFrom + CoraCom.standardValidity
) : BasicEvent(origin, priority, createdAt, session, validFrom, validUpto)
