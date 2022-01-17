/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import java.math.BigDecimal
import java.time.Duration
import java.time.temporal.ChronoUnit

class java_time_DurationConverter : BasicTypeConverter<Duration>(Duration::class) {
    private val translation = mapOf(
        "secs" to ChronoUnit.SECONDS,
        "sec" to ChronoUnit.SECONDS,
        "mins" to ChronoUnit.MINUTES,
        "min" to ChronoUnit.MINUTES,
        "hrs" to ChronoUnit.HOURS,
        "days" to ChronoUnit.DAYS,
        "ns" to ChronoUnit.NANOS,
        "μs" to ChronoUnit.MICROS,
        "us" to ChronoUnit.MICROS,
        "ms" to ChronoUnit.MICROS,
        "s" to ChronoUnit.SECONDS,
        "d" to ChronoUnit.DAYS,
        "h" to ChronoUnit.HOURS,
        "m" to ChronoUnit.MINUTES
    )

    override fun decodeFrom(value: String): Duration? = translation
        .filter { value.endsWith(it.key) }
        .maxByOrNull { it.key.length }
        ?.let { unit ->
            Duration.of(value.removeSuffix(unit.key).trim().toLong(), unit.value)
        }

    override fun convertFrom(value: Any): Duration? = when (value) {
        is BigDecimal -> {
            val nanos = value.stripTrailingZeros().remainder(BigDecimal.ONE).movePointRight(value.scale()).abs().toBigInteger()
            Duration.ofSeconds(value.toLong(), nanos.toLong())
        }
        else -> TODO("Not yet implemented")
    }
}
