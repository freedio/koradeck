package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.IMMEX
import com.coradec.coradeck.ctrl.ctrl.impl.CIMMEX
import com.coradec.coradeck.ctrl.model.*
import com.coradec.coradeck.ctrl.model.impl.BasicAgentPool
import com.coradec.coradeck.ctrl.model.impl.BasicRequestList
import com.coradec.coradeck.ctrl.model.impl.BasicRequestSet
import com.coradec.coradeck.ctrl.model.impl.BasicTask

class CoraControlImpl : CoraControlAPI {
    override val Market: MarketSpace = CentralMarketSpace
    override val IMMEX: IMMEX get() = CIMMEX

    override fun createRequestSet(origin: Origin, vararg requests: Request): RequestSet =
        BasicRequestSet(origin, Sequence { requests.iterator() })

    override fun createRequestList(origin: Origin, vararg requests: Request): RequestList =
        BasicRequestList(origin, Sequence { requests.iterator() })

    override fun <AgentType : Agent> createAgentPool(low: Int, high: Int, generator: () -> AgentType): AgentPool =
        BasicAgentPool(low, high, generator)

    override fun taskOf(executable: Runnable, prio: Priority): Task = BasicTask(executable, prio)
}