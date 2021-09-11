/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.ctrl.IMMEX
import com.coradec.coradeck.ctrl.model.*
import com.coradec.coradeck.dir.model.module.CoraModule

object CoraControl : CoraModule<CoraControlAPI>() {
    val Market: MarketSpace = impl.Market
    val IMMEX: IMMEX = impl.IMMEX

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

    /**
     * Creates and returns an agent pool of the specified agent type, with the specified low and high watermark and agent generator.
     */
    fun <AgentType: Agent> createAgentPool(low: Int, high: Int, generator: () -> AgentType): AgentPool =
        impl.createAgentPool(low, high, generator)

    /** Creates a task from the specified executable with the specified priority. */
    fun taskOf(executable: Runnable, prio: Priority): Task = impl.taskOf(executable, prio)
}
