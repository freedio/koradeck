package com.coradec.coradeck.com.model

interface Recipient: Target {
    fun onMessage(message: Information)
}
