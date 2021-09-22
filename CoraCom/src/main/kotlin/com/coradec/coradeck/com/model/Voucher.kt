/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.core.model.Timespan

interface Voucher<V>: Request {
    val current: V?
    val value: V
    fun value(t: Timespan): V
}
