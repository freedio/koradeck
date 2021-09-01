package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.com.model.State.SUCCESSFUL
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeoutException

open class BasicVoucher<V>(
    origin: Origin,
    urgent: Boolean = false,
    created: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    expires: Expiration = Expiration.never_expires,
    target: Recipient? = null,
    initialValue: V? = null
) : BasicRequest(origin, urgent, created, session, expires, target), Voucher<V> {
    private val valueSemaphore = CountDownLatch(1)
    private var valueSet = false
    override var current: V? = initialValue
    @Suppress("UNCHECKED_CAST")
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

    override fun interceptSetState(state: State) {
        if (state == SUCCESSFUL)
            if (valueSet) valueSemaphore.countDown() else throw IllegalStateException("To be successful, state must be set!")
        super.interceptSetState(state)
    }

    override fun value(t: Timespan): V {
        if (valueSemaphore.await(t.amount, t.unit)) return current!!
        throw TimeoutException()
    }
}