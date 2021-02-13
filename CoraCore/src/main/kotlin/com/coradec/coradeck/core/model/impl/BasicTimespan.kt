package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.Timespan
import java.time.temporal.TemporalUnit
import java.util.concurrent.TimeUnit

data class BasicTimespan(override val amount: Long, override val unit: TimeUnit) : Timespan {
    override val representation = "%d %s".format(amount, unit.name)
}
