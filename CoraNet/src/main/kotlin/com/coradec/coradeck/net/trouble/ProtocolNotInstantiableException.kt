/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.net.trouble

import com.coradec.coradeck.net.model.ProtocolSpecification

class ProtocolNotInstantiableException(val specification: ProtocolSpecification) : BasicNetException() {

}
