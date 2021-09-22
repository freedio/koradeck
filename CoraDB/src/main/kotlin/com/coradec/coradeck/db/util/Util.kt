/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.util

import java.sql.ResultSet
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


fun String.toSqlObjectName() = replace(Regex("([A-Z])"), "_$1").uppercase().trimStart('_')
val <T : Any> KClass<T>.fields: Map<String, KProperty1<T, *>> get() = memberProperties.associateBy { it.name }
fun <T: Any> ResultSet.streamOf(klass: KClass<T>): Stream<T> = StreamSupport.stream(ResultSetSpliterator(this, klass), false)
fun Any?.toSqlFieldValue(): Any? = when (this) {
    is java.sql.Date -> toLocalDate()
    is java.sql.Time -> toLocalTime()
    is java.sql.Timestamp -> toLocalDateTime()
    else -> this
}