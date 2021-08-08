package com.coradec.coradeck.com.model

interface Recipient: Target {
    /** Injects the specified message into the recipient's queue. */
    fun <I: Information> inject(message: I): I
    /** Forwards (a copy of) the specified [already transmitted] message into the recipient's queue. */
    fun <I: Information> forward(message: I): I
}
