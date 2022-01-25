/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.ctrl.module.CoraControl

interface Monitor {
    /** Registers the specified notification with this monitor. */
    fun <I: Information> register(notification: Notification<I>)
    /** Triggers the specified action when all notifications and requests on the monitor have been finished. */
    fun onClear(action: () -> Unit)

    companion object {
        operator fun invoke(): Monitor = CoraControl.Monitor
    }
}
