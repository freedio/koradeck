/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

enum class Priority {
    A0, A1, B0, A2, B1, C0, A3, B2, C1, B3, C2, C3;

    companion object {
        val defaultPriority = B2
    }
}