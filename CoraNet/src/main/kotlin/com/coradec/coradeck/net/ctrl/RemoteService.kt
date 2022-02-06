/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.net.ctrl

import com.coradec.coradeck.net.module.CoraNet
import java.net.InetSocketAddress

interface RemoteService {
    /** The endpoint address and port. */
    val endpoint: InetSocketAddress
    /** The protocol in use. */
    val protocol: Protocol
    /** Whether the remote service is connected. */
    val connected: Boolean

    companion object {
        operator fun invoke(endpoint: InetSocketAddress, protocol: Protocol = Protocol.defaultFor(endpoint.port)): RemoteService = CoraNet.connectRemoteService(endpoint, protocol)
    }
}
