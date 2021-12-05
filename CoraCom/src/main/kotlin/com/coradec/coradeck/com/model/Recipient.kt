/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.trouble.NotificationRejectedException

interface Recipient: Target {
    /** Invoked to check if the recipient accepts the specified notification. */
    fun accepts(notification: Notification<*>): Boolean
    /** Delivers the specified notification to the recipient. */
    @Throws(NotificationRejectedException::class) fun receive(notification: Notification<*>)
}
