/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

interface State: Comparable<State> {
    val name: String
    val rank: Int

    override fun compareTo(other: State): Int = rank - other.rank
}
