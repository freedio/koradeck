/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.impl

import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.model.BusNodeStateTransition
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.core.model.Origin

class BasicNodeStateTransition(
    origin: Origin,
    override val from: BusNodeState,
    override val unto: BusNodeState,
    override val member: MemberView,
    override val context: BusContext?
) : BasicRequest(origin), BusNodeStateTransition
