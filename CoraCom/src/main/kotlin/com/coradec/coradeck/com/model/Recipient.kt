/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.trouble.NotificationRejectedException

interface Recipient: Target {
    /** Has the recipient accept the specified information; returns the message containing the information. */
    fun <I: Information> accept(info: I): Message<I>
    /** Delivers the specified notification to the recipient. */
    @Throws(NotificationRejectedException::class) fun subscribe(notification: Notification<*>)
}
