/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.model

interface HubDelegator: NodeDelegator {
    fun onLoading()
    fun onLoaded()
    fun onUnloading()
    fun onUnloaded()
    fun onJoining(name: String, node: BusNode)
    fun onJoined(name: String, node: BusNode)
    fun onLeaving(name: String, node: BusNode)
    fun onLeft(name: String, node: BusNode)
}
