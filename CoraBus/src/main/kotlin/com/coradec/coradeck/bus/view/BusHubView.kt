/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.view

import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.session.view.View
import kotlin.reflect.KClass

interface BusHubView : View {
    /** The path of the member with the specified name. */
    fun pathOf(name: String): Path
    /** Looks up the superior with the specified type, if there is any. */
    operator fun get(type: Class<*>): MemberView?
    /** Looks up the superior with the specified type, if there is any. */
    operator fun get(type: KClass<*>): MemberView?
    /** Invoked when the specified member is leaving the context. */
    fun onLeaving(member: MemberView)
    /** Invoked when the specified member left the context. */
    fun onLeft(member: MemberView): Boolean

    /** Invoked when the specified candidate is about to join the context. */
    fun onJoining(node: MemberView)
    /** Invoked when the specified member joined the context. */
    fun onJoined(node: MemberView): Boolean

    /** Invoked when the member becomes ready. */
    fun onReady(member: MemberView)
    /** Invoked when the member becomes busy. */
    fun onBusy(member: MemberView)
    /** Invoked when the member, being an engine, has crashed. */
    fun onCrashed(member: MemberView)
    /** Links the new member with the specified name to the hub. */
    fun link(name: String, node: MemberView)
    /** Unlinks the gone member with the specified name from the hub. */
    fun unlink(name: String)
    /** Renames the member with the specified name to the specified new name. */
    fun rename(name: String, newName: String)
}
