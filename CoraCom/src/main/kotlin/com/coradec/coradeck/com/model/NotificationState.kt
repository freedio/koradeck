/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import java.util.*

enum class NotificationState {
    NEW, ENQUEUED, DISPATCHED, DELIVERED, REJECTED, PROCESSED, CRASHED, LOST;

    companion object {
        val TERMINAL: EnumSet<NotificationState> = EnumSet.of(REJECTED, CRASHED, LOST)
    }
}
