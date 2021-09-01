package com.coradec.coradeck.com.model

interface Recipient: Target {
    /** The recipient's queue capacity. */
    val capacity: Int

    /** Injects the specified information to the recipient's message queue (in the IMMEX). */
    fun <I : Information> inject(message: I): I
    /** Delivers the specified message to the recipient. */
    fun onMessage(message: Information)
}
