package com.coradec.coradeck.core.model

import java.time.ZonedDateTime

interface Deferred {
    val executesAt: ZonedDateTime
    val due: Boolean
}