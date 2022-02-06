/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.net.ctrl

import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.net.model.ProtocolSpecification
import com.coradec.coradeck.net.module.CoraNet
import com.coradec.coradeck.net.trouble.UnknownPortException
import java.net.InetSocketAddress

typealias Port=Int
typealias Name=String

interface Protocol {
    /** Checks if the specified endpoint is connected as per the protocol. */
    fun isConnected(endpoint: InetSocketAddress): Boolean

    companion object {
        private val PROP_PROTOCOLS = LocalProperty<Map<Port, ProtocolSpecification>>("Protocols")
        fun defaultFor(port: Int): Protocol = Protocol(PROP_PROTOCOLS.value[port] ?: throw UnknownPortException(port))
        operator fun invoke(spec: ProtocolSpecification): Protocol = CoraNet.getProtocol(spec)
    }
}
