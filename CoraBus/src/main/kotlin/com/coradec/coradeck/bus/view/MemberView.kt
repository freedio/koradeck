/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.view

import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.trouble.ViewNotFoundException
import com.coradec.coradeck.session.view.View
import kotlin.reflect.KClass

interface MemberView : View {
    /** Attaches the member to the specified bus context, returning a request to track progress. */
    fun attach(context: BusContext): Request
    /** Waits until the member is ready. */
    fun standby()
    /** Detaches the member from its current bus context, returning a request to track progress. */
    fun detach(): Request
    /** Looks up the view of the specified type on the member in the context of the specified session. */
    fun <V: View> lookupView(session: Session, type: KClass<V>): V?
    /** Returns the view of the specified type on the member in the context of the specified session, or fails. */
    @Throws(ViewNotFoundException::class)
    fun <V: View> getView(session: Session, type: KClass<V>): V
}
