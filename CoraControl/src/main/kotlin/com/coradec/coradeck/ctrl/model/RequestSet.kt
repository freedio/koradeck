package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.MultiRequest
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.ctrl.module.CoraControl

interface RequestSet: Observer, MultiRequest {
    companion object {
        operator fun invoke(origin: Origin, vararg requests: Request) =
                CoraControl.createRequestSet(origin, *requests)
        operator fun invoke(origin: Origin, requests: Sequence<Request>) =
                CoraControl.createRequestSet(origin, *requests.toList().toTypedArray())
    }
}