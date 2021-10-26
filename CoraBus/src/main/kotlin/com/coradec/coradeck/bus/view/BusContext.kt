/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.view

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.dir.model.Path
import kotlin.reflect.KClass

interface BusContext {
    /** A view on the hub containing the node. */
    val hub: BusHubView
    /** The member, once it joined. */
    val member: BusNode?
    /** The name of the node in the context. */
    val name: String
    /** The path of the element inside the hub. */
    val path: Path get() = hub.pathOf(name)

    /** Looks up a superior of the specified type in the bus hierarchy. Returns null if none was found. */
    operator fun <D : BusNode> get(type: Class<D>): D?
    /** Looks up a superior of the specified type in the bus hierarchy. Returns null if none was found. */
    operator fun <D : BusNode> get(type: KClass<D>): D?
    /** Indicates that the node is about to leave the context. */
    fun leaving()
    /** Indicates that the node has left the context. */
    fun left()
    /** Indicates that the specified node is about to join the context. */
    fun joining(node: BusNode)
    /** Indicates that the specified node has joined the context. */
    fun joined(node: BusNode)
    /** Indicates that the node has become ready. */
    fun ready()
    /** Indicates that the node has become busy (which practically means it is going down). */
    fun busy()
}