/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

interface Message: Event {
    val recipient: Recipient?

    /** Returns this message or a copy with the recipient set to the specified recipient. */
    fun withRecipient(target: Recipient): Message
    /** Returns this message or a copy with the recipient set to the specified recipient, but only if no recipient was set. */
    fun withDefaultRecipient(target: Recipient?): Message
    /** Enqueues this message with the specified target recipient. */
    fun enqueue(target: Recipient)
}
