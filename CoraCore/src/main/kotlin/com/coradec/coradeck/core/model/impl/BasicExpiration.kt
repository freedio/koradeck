package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.Expiration
import java.time.LocalDateTime

data class BasicExpiration(override val at: LocalDateTime): Expiration
