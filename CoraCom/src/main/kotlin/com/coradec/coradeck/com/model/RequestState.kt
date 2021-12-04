/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import java.util.*

enum class RequestState {
    NEW, ENQUEUED, DISPATCHED, DELIVERED, REJECTED, PROCESSED, LOST, SUCCESSFUL, FAILED, CANCELLED;

    companion object {
        val FINISHED: EnumSet<RequestState> = EnumSet.of(SUCCESSFUL, FAILED, CANCELLED, LOST)
    }
}
