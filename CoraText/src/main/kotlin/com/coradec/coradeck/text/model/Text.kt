/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model

interface Text {
    operator fun get(vararg args: Any): String
    val content: String get() = get()
}
