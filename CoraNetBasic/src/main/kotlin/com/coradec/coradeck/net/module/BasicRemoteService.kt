/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.net.module

import com.coradec.coradeck.net.ctrl.Protocol
import com.coradec.coradeck.net.ctrl.RemoteService
import java.net.InetSocketAddress

class BasicRemoteService(override val endpoint: InetSocketAddress, override val protocol: Protocol) : RemoteService {
    override val connected: Boolean get() = protocol.isConnected(endpoint)

}
