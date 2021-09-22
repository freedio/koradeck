/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

interface Prioritized: Comparable<Prioritized> {
    val priority: Priority

    override fun compareTo(other: Prioritized): Int = priority.compareTo(other.priority)
}
