/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model.delegation

import com.coradec.coradeck.bus.view.MemberView

interface HubDelegator: NodeDelegator {
    /** Invoked before the hub is loading its members.  The hub can throw an exception to refuse. */
    fun onLoading()
    /** Invoked when the members are being loaded into the hub.  `true` if successfully loaded, `false` if pending. */
    fun onLoaded(): Boolean
    /** Invoked before the hub is unloading its members.  The hub can throw an exception to refuse. */
    fun onUnloading()
    /** Invoked when the members are being unloaded from the hub.  `true` if successfully unloaded, `false` if pending. */
    fun onUnloaded(): Boolean
    /** Invoked before a member is joining the hub.  The hub can throw an exception to refuse. */
    fun onJoining(name: String, node: MemberView)
    /** Invoked when a member is joining the hub.  `true` if successfully accepted, `false` if pending. */
    fun onJoined(name: String, node: MemberView): Boolean
    /** Invoked before a member is leaving the hub.  The hub can throw an exception to refuse. */
    fun onLeaving(name: String, node: MemberView)
    /** Invoked when a member is leaving the hub.  `true` if successfully dismissed, `false` if pending. */
    fun onLeft(name: String, node: MemberView): Boolean
}
