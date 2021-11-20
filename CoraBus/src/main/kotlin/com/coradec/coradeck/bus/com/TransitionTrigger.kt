/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.com

import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.impl.BasicInformation
import com.coradec.coradeck.core.model.Origin

class TransitionTrigger(
    origin: Origin,
    val trigger: Request,
    val states: Iterator<BusNodeState>,
    val context: BusContext?,
    val memberView: MemberView
): BasicInformation(origin)
