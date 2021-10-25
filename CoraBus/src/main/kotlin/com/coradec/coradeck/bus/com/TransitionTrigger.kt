/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.com

import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Request

interface TransitionTrigger: Information {
    val context: BusContext?
    val states: Sequence<BusNodeState>
    val trigger: Request
}
