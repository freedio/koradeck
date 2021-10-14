/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.impl.BasicRequestStateObserver

interface RequestStateObserver: Observer {
    val state: RequestState
    val action: () -> Unit

    companion object {
        operator fun invoke(state: RequestState, action: () -> Unit): RequestStateObserver = BasicRequestStateObserver(state, action)
    }
}
