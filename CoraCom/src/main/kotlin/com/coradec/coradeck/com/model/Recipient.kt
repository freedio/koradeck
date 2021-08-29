package com.coradec.coradeck.com.model

interface Recipient: Target {
    /** The recipient's queue capacity. */
    val capacity: Int

    /** Delivers the specified message to the recipient. */
    fun onMessage(message: Information)
}
