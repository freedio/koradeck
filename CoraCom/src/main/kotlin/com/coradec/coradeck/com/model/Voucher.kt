/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.core.model.Timespan
import java.util.concurrent.TimeoutException

interface Voucher<V>: Request {
    /** The current state of the value (may be null because it either wasn't set yet, or because V is nullable). */
    val current: V?
    /** The value (calling the getter may require waiting for the value to be set by someone else). */
    var value: V
    /** Like 'value', but waits for at most the specified timespan before failing with a timeout exception or returning the value. */
    @Throws(TimeoutException::class) fun value(t: Timespan): V
    /** Forwards the value into the specified other voucher as soon as it is available, and the termination state as well. */
    fun forwardTo(voucher: Voucher<V>)
    /** Triggers the specified action when the request is finished.  Fluid. */
    fun whenVoucherFinished(action: Voucher<V>.() -> Unit): Voucher<V>
}
