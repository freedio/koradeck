/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.view

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.dir.model.Path
import kotlin.reflect.KClass

interface BusHubView {
    /** The path of the member with the specified name. */
    fun pathOf(name: String): Path
    /** Looks up the superior with the specified type, if there is any. */
    operator fun <D : BusNode> get(type: Class<D>): D?
    /** Looks up the superior with the specified type, if there is any. */
    operator fun <D : BusNode> get(type: KClass<D>): D?
    /** Invoked when the specified member is leaving the context. */
    fun onLeaving(member: BusNode)
    /** Invoked when the specified member left the context. */
    fun onLeft(member: BusNode)
    /** Invoked when the specified candidate is about to join the context. */
    fun onJoining(node: BusNode)
    /** Invoked when the specified member joined the context. */
    fun onJoined(node: BusNode)
    /** Invoked when the member becomes ready. */
    fun onReady(member: BusNode)
    /** Invoked when the member becomes busy. */
    fun onBusy(member: BusNode)
}
