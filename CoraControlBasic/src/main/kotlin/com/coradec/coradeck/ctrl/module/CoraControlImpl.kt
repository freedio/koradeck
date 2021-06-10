package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.ctrl.model.RequestList
import com.coradec.coradeck.ctrl.model.RequestSet
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.ctrl.ctrl.EMS
import com.coradec.coradeck.ctrl.ctrl.impl.CEMS
import com.coradec.coradeck.ctrl.model.impl.BasicRequestList
import com.coradec.coradeck.ctrl.model.impl.BasicRequestSet

class CoraControlImpl : CoraControlAPI {
    override val EMS: EMS get() = CEMS

    override fun createRequestSet(origin: Origin, recipient: Recipient, vararg requests: Request): RequestSet =
            BasicRequestSet(origin, recipient, requests.toList())

    override fun createRequestList(origin: Origin, recipient: Recipient, vararg requests: Request): RequestList =
            BasicRequestList(origin, recipient, requests.toList())
}