/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.bus.view.BusContext
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.trouble.ViewNotFoundException
import com.coradec.coradeck.session.view.View
import kotlin.reflect.KClass

interface NodeDelegator {
    /** The node to which all actions are delegated. */
    val node: MemberView

    /** Invoked before the node is attached to the specified context.  The node can throw an exception to refuse. */
    fun onAttaching(context: BusContext)
    /** Invoked after the node was successfully attached to the specified context. */
    fun onAttached(context: BusContext)
    /** Invoked before the node is going through initialization.  The node may throw an exception if initialization failed. */
    fun onInitializing()
    /** Invoked after the node was successfully initialized. */
    fun onInitialized()
    /** Invoked before the node is going through finalization. */
    fun onFinalizing()
    /** Invoked after the node was successfully finalized. */
    fun onFinalized()
    /** Invoked before the node is detached from its current context.  The node can throw an exception to refuse unless forced. */
    fun onDetaching(forced: Boolean)
    /** Invoked after the node was successfully removed from its former context. */
    fun onDetached()
    /** Invoked after the node reached state READY. */
    fun onReady()
    /** Invoked after the node lost state READ. */
    fun onBusy()
    /** Looks up the view of the specified type in the context of the specified session. */
    fun <V: View> lookupView(session: Session, type: KClass<V>): V?
    /** Returns the view of the specified type in the context of the specified session, or fails with ViewNotFoundException. */
    @Throws(ViewNotFoundException::class)
    fun <V: View> getView(session: Session, type: KClass<V>): V
}
