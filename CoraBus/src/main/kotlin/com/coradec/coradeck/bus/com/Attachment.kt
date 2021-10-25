/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.com

import com.coradec.coradeck.bus.model.BusNodeState
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.com.model.impl.BasicInformation
import com.coradec.coradeck.core.model.Origin

class Attachment(
    origin: Origin,
    override val trigger: AttachRequest,
    override val states: Sequence<BusNodeState>,
    override val context: BusContext?
): BasicInformation(origin), TransitionTrigger {

}
