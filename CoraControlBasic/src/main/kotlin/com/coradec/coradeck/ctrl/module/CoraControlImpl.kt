package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.ctrl.ctrl.IMMEX
import com.coradec.coradeck.ctrl.ctrl.impl.CIMMEX
import com.coradec.coradeck.ctrl.model.CentralMarketSpace
import com.coradec.coradeck.ctrl.model.MarketSpace
import com.coradec.coradeck.ctrl.model.RequestList
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.ctrl.model.impl.BasicRequestList
import com.coradec.coradeck.ctrl.model.impl.BasicRequestSet

class CoraControlImpl : CoraControlAPI {
    override val Market: MarketSpace = CentralMarketSpace
    override val IMMEX: IMMEX get() = CIMMEX

    override fun createRequestSet(origin: Origin, vararg requests: Request): RequestSet =
        BasicRequestSet(origin, Sequence { requests.iterator() })

    override fun createRequestList(origin: Origin, vararg requests: Request): RequestList =
        BasicRequestList(origin, Sequence { requests.iterator() })
}