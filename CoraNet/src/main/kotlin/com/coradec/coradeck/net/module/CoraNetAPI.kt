/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.net.module

import com.coradec.coradeck.module.model.CoraModuleAPI
import com.coradec.coradeck.net.ctrl.Protocol
import com.coradec.coradeck.net.ctrl.RemoteService
import com.coradec.coradeck.net.model.ProtocolSpecification
import java.net.InetSocketAddress

interface CoraNetAPI: CoraModuleAPI {
    /** Creates a remote service connection to the specified endpoint over the specified protocol. */
    fun connectRemoteService(endpoint: InetSocketAddress, protocol: Protocol = Protocol.defaultFor(endpoint.port)): RemoteService
    /** (Creates and) returns a protocol from the specified protocol specification. */
    fun getProtocol(spec: ProtocolSpecification): Protocol
}
