/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.module.db.annot

import kotlin.annotation.AnnotationTarget.*

@Retention(AnnotationRetention.RUNTIME)
@Target(PROPERTY, FIELD, TYPE, VALUE_PARAMETER, TYPE_PARAMETER, VALUE_PARAMETER)
annotation class Generated(
    val type: String = "identity", /* identity | sequence <sequence-name> | fieldexpr */
    val always: Boolean = false
)
