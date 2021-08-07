package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.Expiration
import java.time.ZonedDateTime

data class BasicExpiration(override val at: ZonedDateTime): Expiration
