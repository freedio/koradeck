package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicExpiration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

interface Expiration {
    val at: ZonedDateTime

    companion object {
        val never_expires = BasicExpiration(LocalDateTime.MAX.atZone(ZoneOffset.UTC))
    }
}
