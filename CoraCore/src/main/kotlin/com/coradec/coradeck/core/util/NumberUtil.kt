/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import kotlin.math.abs

val Int.octal: Int get() {
    val negative = this < 0
    var base = 1
    var still = abs(this)
    var result = 0
    while (still != 0) {
        result += base * (still % 10)
        still /= 10
        base *= 8
    }
    return if (negative) -result else result
}