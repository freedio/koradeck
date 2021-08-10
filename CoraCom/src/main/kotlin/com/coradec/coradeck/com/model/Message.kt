package com.coradec.coradeck.com.model

interface Message: Event {
    val recipient: Recipient?

    fun enqueue(target: Recipient)
}
