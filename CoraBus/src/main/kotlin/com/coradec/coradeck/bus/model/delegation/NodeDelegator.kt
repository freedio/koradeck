/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.delegation

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
    /** Invoked when the node is being attached to the specified context.  `true` if successfully attached, `false` if pending. */
    fun onAttached(context: BusContext): Boolean
    /** Invoked before the node is going through initialization.  The node may throw an exception if initialization failed. */
    fun onInitializing()
    /** Invoked when the node is being initialized.  `true` if successfully initialized, `false` if pending. */
    fun onInitialized(): Boolean
    /** Invoked before the node is going through finalization. */
    fun onFinalizing()
    /** Invoked when the node is being finalized.  `true` if successfully finalized, `false` if pending. */
    fun onFinalized(): Boolean
    /** Invoked before the node is detached from its current context.  The node can throw an exception to refuse unless forced. */
    fun onDetaching(forced: Boolean)
    /** Invoked when the node is being removed from its former context.  `true` if successfully detached, `false` if pending. */
    fun onDetached(): Boolean
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
