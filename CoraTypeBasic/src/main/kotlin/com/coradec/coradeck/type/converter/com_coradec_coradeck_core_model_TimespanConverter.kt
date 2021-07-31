/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import java.time.Duration
import java.util.concurrent.TimeUnit.*

class com_coradec_coradeck_core_model_TimespanConverter : BasicTypeConverter<Timespan>(Timespan::class) {
    private val translation = mapOf(
        "secs" to SECONDS,
        "sec" to SECONDS,
        "mins" to MINUTES,
        "min" to MINUTES,
        "hrs" to HOURS,
        "days" to DAYS,
        "ns" to NANOSECONDS,
        "μs" to MICROSECONDS,
        "us" to MICROSECONDS,
        "ms" to MICROSECONDS,
        "s" to SECONDS,
        "d" to DAYS,
        "h" to HOURS,
        "m" to MINUTES
    )

    override fun convertFrom(value: Any): Timespan? = when (value) {
        is Duration -> TODO()
        else -> null
    }

    override fun decodeFrom(value: String): Timespan? = translation
        .filter { value.endsWith(it.key) }
        .maxByOrNull { it.key.length }
        ?.let { unit ->
            Timespan(value.removeSuffix(unit.key).trim().toLong(), unit.value)
        }
}
