package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch

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
    override var current: V? = initialValue
    override var value: V?
        get() = valueSemaphore.await().let { current }
        set(value) {
            current = value.also { valueSemaphore.countDown() }
        }

    init {
        @Suppress("LeakingThis")
        if (initialValue != null) value = initialValue
    }
}