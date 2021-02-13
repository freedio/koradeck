package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicExpiration
import java.time.LocalDateTime

interface Expiration {
    val at: LocalDateTime

    companion object {
        val never_expires = BasicExpiration(LocalDateTime.MAX)
    }
}
