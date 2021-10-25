/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.dir.model.Path
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

interface BusNode : Origin, Recipient {
    /** The state of the node. */
    val state: BusNodeState
    /** The states the bus node already had and has. */
    val states: List<BusNodeState>
    /** If the node is ready. */
    val ready: Boolean
    /** The bus context. Absent if not attached */
    val context: BusContext?
    /** If this node is attached to a bus context. */
    val attached: Boolean get() = context != null
    /** The directory path of this node.  Absent if unattached. */
    val path: Path?
    /** The name of this node in its context.  Absent if unattached. */
    val name: String?

    /** Attaches this node to the specified bus context and initializaes it. */
    fun attach(context: BusContext): Request
    /** Terminates this node and detaches it from the specified bus context. */
    fun detach(): Request
    /** The bus context.  Will wait up to [timeout] [timeoutUnit]s for the node to be connected, then throw TimeoutException. */
    @Throws(TimeoutException::class) fun context(timeout: Long, timeoutUnit: TimeUnit): BusContext
}
