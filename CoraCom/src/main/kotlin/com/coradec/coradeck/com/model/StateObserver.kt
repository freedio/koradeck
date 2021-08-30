package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.impl.BasicStateObserver

interface StateObserver: Observer {
    val state: State
    val action: () -> Unit

    companion object {
        operator fun invoke(state: State, action: () -> Unit): StateObserver = BasicStateObserver(state, action)
    }
}
