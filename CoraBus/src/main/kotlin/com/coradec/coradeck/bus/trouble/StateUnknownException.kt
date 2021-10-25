/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.trouble

import com.coradec.coradeck.bus.model.BusNodeState

class StateUnknownException(val state: BusNodeState) : BusException() {

}
