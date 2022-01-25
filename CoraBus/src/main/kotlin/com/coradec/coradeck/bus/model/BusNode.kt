/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.bus.model.BusNodeState.INITIALIZED
import com.coradec.coradeck.bus.trouble.NodeNotAttachedException
import com.coradec.coradeck.bus.trouble.StateUnreachableException
import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.caller
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.session.model.Session
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
    /** If this node is initialized. */
    val initialized: Boolean get() = INITIALIZED in states
    /** The directory path of this node.  Absent if unattached. */
    val path: Path?
    /** The name of this node in its context.  Absent if unattached. */
    val name: String?
    /** The member view of the current session. */
    val memberView: MemberView

    /** Returns a view on the member in the context of the specified session. */
    fun memberView(session: Session): MemberView

    /** Attaches this node to the specified bus context and initializaes it. */
    fun attach(origin: Origin = caller, context: BusContext): Request
    /** Terminates this node and detaches it from the specified bus context. */
    fun detach(origin: Origin = caller): Request
    /** Changes the name of node to the specified name. */
    @Throws(NodeNotAttachedException::class) fun renameTo(name: String)
    /** The bus context.  Will wait up to [timeout] [timeoutUnit]s for the node to be connected, then throw TimeoutException. */
    @Throws(TimeoutException::class) fun context(timeout: Long, timeoutUnit: TimeUnit): BusContext
    /** Waits for at most [delay] until the node has reached the specified state.  Fails if the specified state is unreachable. */
    @Throws(StateUnreachableException::class, TimeoutException::class) fun standby(delay: Timespan, state: BusNodeState)
    /** Waits until the node has reached the specified state.  Fails if the specified state is unreachable. */
    @Throws(StateUnreachableException::class) fun standby(state: BusNodeState)
    /** Waits for at most [delay] until the node is ready.  Fails if the node is shutting down already. */
    @Throws(StateUnreachableException::class, TimeoutException::class) fun standby(delay: Timespan)
    /** Executes the specified action when the specified state is reached. */
    fun onState(state: BusNodeState, action: BusNode.() -> Unit)
}
