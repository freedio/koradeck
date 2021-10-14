/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.RequestState
import com.coradec.coradeck.com.model.RequestStateObserver

class BasicRequestStateObserver(override val state: RequestState, override val action: () -> Unit) : RequestStateObserver {
    override fun onNotification(event: Event): Boolean = when (event) {
        is RequestStateChangedEvent ->
            if (event.current == state) {
                action.invoke()
                true
            } else false
        else -> false
    }
}
