/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.com.trouble.RequestCancelledException
import com.coradec.coradeck.com.trouble.RequestFailedException
import com.coradec.coradeck.core.annot.NonRepresentable
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.session.model.Session
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeoutException

@Suppress("UNCHECKED_CAST")
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

    init {
        onSuccess { if (valueSet) valueSemaphore.countDown() else throw IllegalStateException("Value has not been set!") }
        onFailure { valueSemaphore.countDown() }
        onCancellation { valueSemaphore.countDown() }
    }

    @Suppress("UNCHECKED_CAST")
    @NonRepresentable
    override var value: V
        get() = lookup()
        set(value) {
            if (complete) throw IllegalStateException("Voucher already complete!")
            current = value
            valueSet = true
        }

    init {
        @Suppress("LeakingThis")
        if (initialValue != null) value = initialValue
    }

    private fun lookup(): V = when (state) {
        SUCCESSFUL -> current as V
        FAILED -> throw reason ?: RequestFailedException()
        CANCELLED -> throw RequestCancelledException()
        else -> valueSemaphore.await().let {
            when (state) {
                SUCCESSFUL -> current as V
                FAILED -> throw RequestFailedException(reason)
                CANCELLED -> throw RequestCancelledException()
                else -> lookup()
            }
        }
    }

    override fun value(t: Timespan): V {
        if (valueSemaphore.await(t.amount, t.unit)) return current!!
        throw TimeoutException()
    }

    override fun forwardTo(voucher: Voucher<V>) {
        whenFinished {
            if (valueSet) voucher.value = current as V
            when (state) {
                SUCCESSFUL -> voucher.succeed()
                FAILED -> voucher.fail(reason)
                CANCELLED -> voucher.cancel(reason)
                else -> relax()
            }
        }
    }

    override fun whenVoucherFinished(action: Voucher<V>.() -> Unit): Voucher<V> = also {
        whenFinished { action.invoke(this@BasicVoucher) }
    }
}
