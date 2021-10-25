/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.com

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.core.model.Origin

class AddMemberRequest(origin: Origin, val name: String, val node: BusNode) : BasicRequest(origin)
