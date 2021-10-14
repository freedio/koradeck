/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val Duration.pretty: CharSequence
    get() {
        val collector = StringBuilder()
        val nanoset = toNanos()
        val nanos = nanoset % 1_000_000 % 1000
        val micros = nanoset / 1_000 % 1000
        val millis = nanoset / 1_000_000 % 1000
        val seconds = nanoset / 1_000_000_000 % 60
        val minutes = nanoset / 60_000_000_000 % 60
        val hours = nanoset / 3_600_000_000_000
        if (hours != 0L) collector += "${hours}h "
        if (minutes != 0L) collector += "${minutes}m "
        if (seconds != 0L) collector += "${seconds}s "
        if (millis != 0L) collector += "${millis}ms "
        if (micros != 0L) collector += "${micros}μs "
        if (nanos != 0L) collector += "${nanos}ns"
        return collector.trim()
    }

operator fun StringBuilder.plusAssign(s: String) { append(s) }
fun String.asLocalDate(pattern: String = "yyyy-MM-dd"): LocalDate =
    LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))
fun String.asLocalDateTime(pattern: String = "yyyy-MM-dd'T'HH:mm:ss"): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
fun max(a: ZonedDateTime, b: ZonedDateTime): ZonedDateTime = if (a.isAfter(b)) a else b
