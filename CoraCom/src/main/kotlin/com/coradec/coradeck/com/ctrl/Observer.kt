/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.ctrl

import com.coradec.coradeck.com.model.Event

@FunctionalInterface
interface Observer {
    /**
     * Notifies the observer about the specified event.
     * @return `true` to automatically deregister the observer (one-shot notification), `false` to keep the observer observing.
     */
    fun notify(event: Event): Boolean = synchronized(this) { onNotification(event) }

    /**
     * Implementation of the notification.
     * @return `true` to automatically deregister the observer (one-shot notification), `false` to keep the observer observing.
     */
    fun onNotification(event: Event): Boolean
}