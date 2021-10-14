/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.RequestState
import com.coradec.coradeck.com.model.RequestState.SUCCESSFUL
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.core.annot.NonRepresentable
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeoutException

open class BasicVoucher<V>(
    origin: Origin,
    priority: Priority = defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC),
    initialValue: V? = null
) : BasicRequest(origin, priority, createdAt, session, validFrom, validUpto), Voucher<V> {
    private val valueSemaphore = CountDownLatch(1)
    private var valueSet = false
    override var current: V? = initialValue
    @Suppress("UNCHECKED_CAST")
    @NonRepresentable
    override var value: V
        get() = valueSemaphore.await().let { current as V }
        set(value) {
            if (complete) throw IllegalStateException("Voucher already complete!")
            current = value
            valueSet = true
        }

    init {
        @Suppress("LeakingThis")
        if (initialValue != null) value = initialValue
    }

    override fun interceptSetState(state: RequestState) {
        if (state == SUCCESSFUL)
            if (valueSet) valueSemaphore.countDown() else throw IllegalStateException("To be successful, value must be set first!")
        super.interceptSetState(state)
    }

    override fun value(t: Timespan): V {
        if (valueSemaphore.await(t.amount, t.unit)) return current!!
        throw TimeoutException()
    }
}