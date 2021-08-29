package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicTimespan
import java.util.concurrent.TimeUnit

interface Timespan: Representable {
    val amount: Long
    val unit: TimeUnit

    companion object {
        operator fun invoke(amount: Long, unit: TimeUnit): Timespan = BasicTimespan(amount, unit)
    }
}
