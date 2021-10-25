/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.com

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.com.model.impl.BasicVoucher
import com.coradec.coradeck.core.model.Origin

class ReplaceMemberVoucher(origin: Origin, val name: String, val substitute: BusNode) : BasicVoucher<BusNode>(origin)
