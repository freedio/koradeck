package com.coradec.coradeck.com.model

interface Recipient: Target {
    /** Injects the specified message into the recipient's queue. */
    fun <I: Information> inject(message: I): I
//    /** Processes the next message when it arrives. */
//    fun onMessage(message: Information)
}
