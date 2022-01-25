/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.impl.BasicStateObserver
import java.util.*

interface StateObserver: Observer {
    val states: EnumSet<NotificationState>
    val action: () -> Unit

    companion object {
        operator fun invoke(action: () -> Unit, vararg state: NotificationState): StateObserver =
            BasicStateObserver(action, EnumSet.copyOf(state.toSet()))
        operator fun invoke(action: () -> Unit, states: EnumSet<NotificationState>): StateObserver =
            BasicStateObserver(action, states)
    }
}
