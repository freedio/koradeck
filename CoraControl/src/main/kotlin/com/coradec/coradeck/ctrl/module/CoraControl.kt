/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.AgentPool
import com.coradec.coradeck.ctrl.ctrl.EMS
import com.coradec.coradeck.ctrl.model.MarketSpace
import com.coradec.coradeck.ctrl.model.RequestList
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.dir.model.module.CoraModule

object CoraControl : CoraModule<CoraControlAPI>() {
    val Market: MarketSpace = impl.Market
    val EMS: EMS = impl.EMS

    /**
     * Creates a request set for the specified requests; injecting the set will trigger all requests at once and be successful
     * only if all requests were successful.  If at least one request failed, the pool fails; if at least one request was cancelled,
     * the set is cancelled, with the effect of cancelling all other requests which are not yet finished.
     */
    fun createRequestSet(origin: Origin, vararg requests: Request): RequestSet =
            impl.createRequestSet(origin, *requests)

    /**
     * Creates a request list for the specified requests; injecting the list will trigger each request in order, waiting for the
     * next until the first has terminated.  If all requests were successful, the list is successful; if any request fails, the
     * rest is skipped and the list fails; if any request is cancelled, the rest is skipped and the list is cancelled.
     */
    fun createRequestList(origin: Origin, vararg requests: Request): RequestList =
            impl.createRequestList(origin, *requests)

    /** Creates a pool of agents of type A with a predefined minimum and maximum pool size and the specified agent creator funtion. */
    fun <A: Agent> createAgentPool(lowWaterMark: Int, highWaterMark: Int, newAgent: () -> A): AgentPool<A> =
        impl.createAgentPool(lowWaterMark, highWaterMark, newAgent)
}
