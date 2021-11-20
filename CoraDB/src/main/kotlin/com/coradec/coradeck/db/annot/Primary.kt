/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.annot

import kotlin.annotation.AnnotationTarget.*

@Retention(AnnotationRetention.RUNTIME)
@Target(PROPERTY, FIELD, TYPE, VALUE_PARAMETER, TYPE_PARAMETER, VALUE_PARAMETER)
annotation class Primary()
