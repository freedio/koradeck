package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.model.impl.BasicMessage

interface Message: Event {
    val recipient: Recipient?

    /** Returns this message or a copy with the recipient set to the specified recipient. */
    override fun withRecipient(target: Recipient): Message
    /** Returns this message or a copy with the recipient set to the specified recipient, but only if no recipient was set. */
    override fun withDefaultRecipient(target: Recipient?): Message
    /** Enqueues this message with the specified target recipient. */
    fun enqueue(target: Recipient)
    /** Creates a copy of this message with the specified recipient. */
    fun copy(recipient: Recipient): BasicMessage
}
