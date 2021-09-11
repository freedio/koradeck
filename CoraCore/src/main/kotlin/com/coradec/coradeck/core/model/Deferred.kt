package com.coradec.coradeck.core.model

import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

interface Deferred : Prioritized {
    /** When the deferred item is due. */
    val due: ZonedDateTime
    /** The due time as an instant. */
    val instant: Instant get() = due.toInstant()
    /** How many milliseconds to wait until the deferred item is due. */
    val delayMs: Long get() = Instant.now().until(due, ChronoUnit.MILLIS).coerceAtLeast(0)
    /** Indicates whether there is a delay > 0. */
    val deferred: Boolean get() = delayMs > 0

    fun compareTo(other: Deferred): Int =
        instant.compareTo(other.instant).let { if (it == 0) compareTo(other as Prioritized) else it }
}