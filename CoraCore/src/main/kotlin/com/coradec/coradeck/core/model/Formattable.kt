/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

interface Formattable {
    fun format(known: Set<Any?>): String
}
