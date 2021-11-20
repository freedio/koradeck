/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.view

import com.coradec.coradeck.dir.model.Path
import kotlin.reflect.KClass

interface BusContext {
    /** A view on the hub containing the node. */
    val hub: BusHubView
    /** The member, once it joined. */
    val member: MemberView?
    /** The name of the node in the context. */
    val name: String
    /** The path of the element inside the hub. */
    val path: Path get() = hub.pathOf(name)

    /** Looks up a superior of the specified type in the bus hierarchy. Returns null if none was found. */
    operator fun get(type: Class<*>): MemberView?
    /** Looks up a superior of the specified type in the bus hierarchy. Returns null if none was found. */
    operator fun get(type: KClass<*>): MemberView?
    /** Indicates that the node is about to leave the context. */
    fun leaving()
    /** Indicates that the node has left the context. */
    fun left()
    /** Indicates that the specified node is about to join the context. */
    fun joining(node: MemberView)
    /** Indicates that the specified node has joined the context. */
    fun joined(node: MemberView)
    /** Indicates that the node has become ready. */
    fun ready()
    /** Indicates that the node has become busy (which practically means it is going down). */
    fun busy()
    /** Indicates that the node, being an engine, has crashed. */
    fun crashed()
    /** Changes the member name to the specified name. */
    fun renameTo(name: String)
}
