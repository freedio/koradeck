/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.Timespan
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.*

data class BasicTimespan(override val amount: Long, override val unit: TimeUnit) : Timespan {
    override val representation = "%d %s".format(amount, unit.name)

    override fun toString() = "%d %s".format(amount, unit.code)

    private val TimeUnit.code: String get() = when(this) {
        SECONDS -> "s"
        NANOSECONDS -> "ns"
        MICROSECONDS -> "μs"
        MILLISECONDS -> "ms"
        MINUTES -> "m"
        HOURS -> "h"
        DAYS -> "d"
    }
}
