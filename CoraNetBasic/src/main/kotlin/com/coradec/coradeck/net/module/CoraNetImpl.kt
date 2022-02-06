/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.net.module

import com.coradec.coradeck.net.ctrl.Name
import com.coradec.coradeck.net.ctrl.Protocol
import com.coradec.coradeck.net.ctrl.RemoteService
import com.coradec.coradeck.net.model.ProtocolSpecification
import com.coradec.coradeck.net.trouble.ProtocolNotInstantiableException
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.full.primaryConstructor

class CoraNetImpl : CoraNetAPI {
    private val protocols = ConcurrentHashMap<Name, Protocol>()

    override fun connectRemoteService(endpoint: InetSocketAddress, protocol: Protocol): RemoteService =
        BasicRemoteService(endpoint, protocol)

    override fun getProtocol(spec: ProtocolSpecification): Protocol =
        protocols.computeIfAbsent(spec.name) {
            spec.klass.primaryConstructor?.call(spec) ?: throw ProtocolNotInstantiableException(spec)
        }
}
