/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.com

import com.coradec.coradeck.com.model.impl.BasicEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.ctrl.model.State

class MachineStateChangedEvent(origin: Origin, val previous: State, val next: State) : BasicEvent(origin)
