package com.coradec.coradeck.com.ctrl

import com.coradec.coradeck.com.model.Event

interface Observer {
    /**
     * Notifies the observer about the specified event.
     * @return `true` to automatically deregister the observer (one-shot notification), `false` to keep the observer observing.
     */
    fun notify(event: Event): Boolean
}