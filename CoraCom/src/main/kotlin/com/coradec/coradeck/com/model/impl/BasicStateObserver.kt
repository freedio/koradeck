/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.NotificationState
import com.coradec.coradeck.com.model.StateObserver

class BasicStateObserver(override val state: NotificationState, override val action: () -> Unit) : StateObserver {
    override fun onNotification(event: Event): Boolean = when (event) {
        is StateChangedEvent ->
            if (event.current == state) {
                action.invoke()
                true
            } else false
        else -> false
    }
}
