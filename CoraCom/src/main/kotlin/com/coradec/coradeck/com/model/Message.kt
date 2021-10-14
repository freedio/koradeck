/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.model.impl.BasicMessage
import com.coradec.coradeck.com.model.impl.TargetedNotification

interface Message<I: Information>: Notification<I> {
    /** The designated recipient. */
    val recipient: Recipient

    companion object {
        operator fun <I: Information> invoke(content: I, recipient: Recipient): Message<I> = BasicMessage(content, recipient)
        operator fun <I: Information> invoke(notification: Notification<I>, recipient: Recipient): Message<I> = TargetedNotification(notification, recipient)
    }
}

infix fun <I: Information> Notification<I>.unto(recipient: Recipient): Message<I> = TargetedNotification(this, recipient)
infix fun <I: Information> I.unto(recipient: Recipient): Message<I> = BasicMessage(this, recipient)
