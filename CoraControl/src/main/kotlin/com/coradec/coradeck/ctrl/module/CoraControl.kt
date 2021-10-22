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
import com.coradec.coradeck.ctrl.model.AgentPool
import com.coradec.coradeck.ctrl.model.MarketSpace
import com.coradec.coradeck.ctrl.model.Task
import com.coradec.coradeck.module.model.CoraModule
import kotlin.reflect.KClass

object CoraControl : CoraModule<CoraControlAPI>() {
    val Market: MarketSpace = impl.Market
    val IMMEX: IMMEX = impl.IMMEX

    /**
     * Creates a request set for the specified requests; injecting the set will trigger all requests at once and be successful
     * only if all requests were successful.  If at least one request failed, the set fails; if at least one request was cancelled,
     * the set is cancelled, with the effect of cancelling all other requests which are not yet finished.
     */
    fun createRequestSet(origin: Origin, vararg requests: Request, processor: Recipient? = null) =
        impl.createRequestSet(origin, requests.asSequence(), processor = processor)

    /**
     * Creates a request set for the specified requests; injecting the set will trigger all requests at once and be successful
     * only if all requests were successful.  If at least one request failed, the set fails; if at least one request was cancelled,
     * the set is cancelled, with the effect of cancelling all other requests which are not yet finished.
     */
    fun createRequestSet(origin: Origin, requests: Sequence<Request>, processor: Recipient? = null) =
        impl.createRequestSet(origin, requests, processor)

    /**
     * Creates a request list for the specified requests; injecting the list will trigger each request in order, waiting for the
     * next until the first has terminated.  If all requests were successful, the list is successful; if any request fails, the
     * rest is skipped and the list fails; if any request is cancelled, the rest is skipped and the list is cancelled.
     */
    fun createRequestList(origin: Origin, requests: Sequence<Request>, processor: Recipient? = null) =
        impl.createRequestList(origin, requests, processor = processor)

    /**
     * Creates a request list for the specified requests; injecting the list will trigger each request in order, waiting for the
     * next until the first has terminated.  If all requests were successful, the list is successful; if any request fails, the
     * rest is skipped and the list fails; if any request is cancelled, the rest is skipped and the list is cancelled.
     */
    fun createRequestList(origin: Origin, vararg requests: Request, processor: Recipient? = null) =
        impl.createRequestList(origin, requests.asSequence(), processor = processor)

    /**
     * Creates a request list for the specified requests; injecting the list will trigger each request in order, waiting for the
     * next until the first has terminated.  If all requests were successful, the list is successful; if any request fails, the
     * rest is skipped and the list fails; if any request is cancelled, the rest is skipped and the list is cancelled.
     */
    fun createRequestList(origin: Origin, requests: List<Request>, processor: Recipient? = null) =
        impl.createRequestList(origin, requests.asSequence(), processor = processor)

    /**
     * Creates a set for the specified infos; injecting the set will trigger all infos at once and be successful
     * only if all infos were successful.  If at least one information failed, the set fails; if at least one information was
     * cancelled, the set is cancelled, with the effect of cancelling all other (cancellable) infos which are not yet finished.
     */
    fun createItemSet(origin: Origin, vararg items: Information, processor: Recipient? = null) =
        impl.createItemSet(origin, items.asSequence(), processor = processor)

    /**
     * Creates a set for the specified infos; injecting the set will trigger all infos at once and be successful
     * only if all infos were successful.  If at least one information failed, the set fails; if at least one information was
     * cancelled, the set is cancelled, with the effect of cancelling all other (cancellable) infos which are not yet finished.
     */
    fun createItemSet(origin: Origin, items: Sequence<Information>, processor: Recipient? = null) =
        impl.createItemSet(origin, items, processor)

    /**
     * Creates a set for the specified infos; injecting the set will trigger all infos at once and be successful
     * only if all infos were successful.  If at least one information failed, the set fails; if at least one information was
     * cancelled, the set is cancelled, with the effect of cancelling all other (cancellable) infos which are not yet finished.
     */
    fun createItemSet(origin: Origin, items: Collection<Information>, processor: Recipient? = null) =
        impl.createItemSet(origin, items.asSequence(), processor)

    /**
     * Creates a list for the specified infos; injecting the list will trigger each information in order, waiting for the
     * next until the first has terminated.  If all infos were successful, the list is successful; if any information fails, the
     * rest is skipped and the list fails; if any request is cancelled, the rest is skipped and the list is cancelled.
     */
    fun createItemList(origin: Origin, vararg items: Information, processor: Recipient? = null) =
        impl.createItemList(origin, items.asSequence(), processor = processor)

    /**
     * Creates and returns an agent pool of the specified agent type, with the specified low and high watermark and agent generator.
     */
    fun <AgentType : Agent> createAgentPool(low: Int, high: Int, generator: () -> AgentType): AgentPool =
        impl.createAgentPool(low, high, generator)

    /** Creates a task from the specified executable with the specified priority. */
    fun taskOf(executable: Runnable, prio: Priority): Task = impl.taskOf(executable, prio)
}

/** Requests to receive the specified type of information from the CIMMEX. */
fun Recipient.receive(vararg types: KClass<out Information>) = types.forEach { CoraControl.IMMEX.plugin(it, this) }

/** Ignore further information from the CIMMEX. */
fun Recipient.ignore() = CoraControl.IMMEX.unplug(this)
