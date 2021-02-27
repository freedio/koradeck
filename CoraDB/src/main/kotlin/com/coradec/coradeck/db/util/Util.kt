package com.coradec.coradeck.db.util

import java.sql.ResultSet
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


fun String.toSqlObjectName() = replace(Regex("([A-Z])"), "_$1").toUpperCase().trimStart('_')
val <T : Any> KClass<T>.fields: Map<String, KProperty1<T, *>> get() = memberProperties.map { Pair(it.name, it) }.toMap()
fun <T: Any> ResultSet.streamOf(klass: KClass<T>): Stream<T> = StreamSupport.stream(ResultSetSpliterator(this, klass), false)
fun Any?.toSqlFieldValue(): Any? = when (this) {
    is java.sql.Date -> toLocalDate()
    is java.sql.Time -> toLocalTime()
    is java.sql.Timestamp -> toLocalDateTime()
    else -> this
}