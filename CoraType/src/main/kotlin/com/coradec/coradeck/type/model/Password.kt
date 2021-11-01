/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.model

import com.coradec.coradeck.type.module.CoraType

interface Password {
    val decoded: String

    companion object {
        operator fun invoke(clearText: String): Password = CoraType.password(clearText)
    }
}
