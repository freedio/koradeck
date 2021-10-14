/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.IMMEX
import com.coradec.coradeck.ctrl.ctrl.impl.CIMMEX
import com.coradec.coradeck.ctrl.model.AgentPool
import com.coradec.coradeck.ctrl.model.CentralMarketSpace
import com.coradec.coradeck.ctrl.model.MarketSpace
import com.coradec.coradeck.ctrl.model.Task
import com.coradec.coradeck.ctrl.model.impl.*

class CoraControlImpl : CoraControlAPI {
    override val Market: MarketSpace = CentralMarketSpace
    override val IMMEX: IMMEX get() = CIMMEX

    override fun createRequestSet(origin: Origin, requests: Sequence<Request>, processor: Recipient?) =
        BasicRequestSet(origin, requests, processor = processor)
    override fun createRequestList(origin: Origin, requests: Sequence<Request>, processor: Recipient?) =
        BasicRequestList(origin, requests, processor = processor)
    override fun createItemSet(origin: Origin, items: Sequence<Information>, processor: Recipient?) =
        BasicItemSet(origin, items, processor = processor)
    override fun createItemList(origin: Origin, items: Sequence<Information>, processor: Recipient?) =
        BasicItemList(origin, items, processor = processor)

    override fun <AgentType : Agent> createAgentPool(low: Int, high: Int, generator: () -> AgentType): AgentPool =
        BasicAgentPool(low, high, generator)

    override fun taskOf(executable: Runnable, prio: Priority): Task = BasicTask(executable, prio)
}