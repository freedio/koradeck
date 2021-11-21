/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.core.model.Timespan
import com.coradec.coradeck.session.model.Session
import java.util.concurrent.TimeoutException

interface Voucher<V: Any?>: Request {
    /** The current state of the value (may be null because it either wasn't set yet, or because V is nullable). */
    val current: V?
    /** The value (calling the getter may require waiting for the value to be set by someone else). */
    var value: V
    /** Like 'value', but waits for at most the specified timespan before failing with a timeout exception or returning the value. */
    @Throws(TimeoutException::class) fun value(t: Timespan): V
    /** Forwards the value and termination state into the specified other voucher as soon as it is available. */
    fun forwardTo(voucher: Voucher<Any?>)
    /** Forwards the value, transformed by the specified transformation, and termination state into the specified other voucher. */
    fun <X> forwardAs(voucher: Voucher<X>, transform: (V, Session) -> X )
    /** Triggers the specified action when the request is finished.  Fluid. */
    fun whenVoucherFinished(action: Voucher<V>.() -> Unit): Voucher<V>
}
