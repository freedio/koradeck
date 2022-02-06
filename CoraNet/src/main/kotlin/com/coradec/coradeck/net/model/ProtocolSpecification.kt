/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.net.model

import com.coradec.coradeck.net.ctrl.Protocol
import kotlin.reflect.KClass

interface ProtocolSpecification {
    /** The (short) name of the protocol, as in "TCP". */
    val id: String
    /** The (long) name of the protocol, as in "Transmission Control Protocol". */
    val name: String
    /** KClass of the implementation. */
    val klass: KClass<out Protocol>
}
