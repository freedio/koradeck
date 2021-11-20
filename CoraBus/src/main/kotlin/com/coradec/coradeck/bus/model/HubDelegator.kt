/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

import com.coradec.coradeck.bus.view.MemberView

interface HubDelegator: NodeDelegator {
    fun onLoading()
    fun onLoaded()
    fun onUnloading()
    fun onUnloaded()
    fun onJoining(name: String, node: MemberView)
    fun onJoined(name: String, node: MemberView)
    fun onLeaving(name: String, node: MemberView)
    fun onLeft(name: String, node: MemberView)
}
