/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.IMMEX
import com.coradec.coradeck.ctrl.ctrl.impl.CIMMEX
import com.coradec.coradeck.ctrl.model.*
import com.coradec.coradeck.ctrl.model.impl.*

class CoraControlImpl : CoraControlAPI {
    override val IMMEX: IMMEX get() = CIMMEX
    override val Market: MarketSpace = CentralMarketSpace
    override val Monitor: Monitor get() = BasicMonitor()

    override fun createRequestSet(origin: Origin, requests: Sequence<Request>, processor: Agent?) =
        BasicRequestSet(origin, requests, processor = processor)
    override fun createRequestList(origin: Origin, requests: Sequence<Request>, processor: Agent?) =
        BasicRequestList(origin, requests, processor = processor)
    override fun createItemSet(origin: Origin, items: Sequence<Information>, processor: Agent?) =
        BasicItemSet(origin, items, processor = processor)
    override fun createItemList(origin: Origin, items: Sequence<Information>, processor: Agent?) =
        BasicItemList(origin, items, processor = processor)

    override fun <AgentType : Agent> createAgentPool(low: Int, high: Int, generator: () -> AgentType): AgentPool =
        BasicAgentPool(low, high, generator)

    override fun taskOf(executable: Runnable, prio: Priority): Task = BasicTask(executable, prio)
}
