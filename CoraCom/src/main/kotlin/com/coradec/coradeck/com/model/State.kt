package com.coradec.coradeck.com.model

import java.util.*

enum class State {
    NEW, ENQUEUED, DISPATCHED, SUCCESSFUL, FAILED, CANCELLED;

    companion object {
        val FINISHED: EnumSet<State> = EnumSet.of(SUCCESSFUL, FAILED, CANCELLED)
    }
}
