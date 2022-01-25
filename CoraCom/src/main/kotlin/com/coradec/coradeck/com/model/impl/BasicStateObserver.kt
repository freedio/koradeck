/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.com.model.NotificationState
import com.coradec.coradeck.com.model.StateObserver
import java.util.*

class BasicStateObserver(override val action: () -> Unit, override val states: EnumSet<NotificationState>) : StateObserver {
    override fun onNotification(event: Event): Boolean = when (event) {
        is StateChangedEvent ->
            if (event.current in states) {
                action.invoke()
                true
            } else false
        else -> false
    }
}
