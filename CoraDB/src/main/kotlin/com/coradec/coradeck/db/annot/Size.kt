package com.coradec.module.db.annot

import kotlin.annotation.AnnotationTarget.*

@Retention(AnnotationRetention.RUNTIME)
@Target(PROPERTY, FIELD, TYPE, VALUE_PARAMETER, TYPE_PARAMETER, VALUE_PARAMETER)
annotation class Size(val value: Int)
