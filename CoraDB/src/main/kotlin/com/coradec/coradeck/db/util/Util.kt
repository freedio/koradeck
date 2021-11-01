/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.util

import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.contains
import com.coradec.module.db.annot.Size
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

fun String.toSqlObjectName(): String = this
    .replace(Regex("([A-Z])"), "_$1")
    .replace(Regex("[.]"), "_")
    .replace(Regex("_+"), "_")
    .uppercase()
    .trimStart('_')

fun <T : Any> KClass<T>.toSqlTableName(): String = classname.toSqlObjectName()
val <T : Any> KClass<T>.fields: Map<String, KProperty1<T, *>> get() = memberProperties.associateBy { it.name }
fun <T : Any> ResultSet.streamOf(klass: KClass<T>): Stream<T> = StreamSupport.stream(ResultSetSpliterator(this, klass), false)
fun <T : Any> ResultSet.asSequence(model: KClass<T>): Sequence<T> = Sequence { ResultSetIterator(this, model) }
fun Any?.toSqlFieldValue(): Any? = when (this) {
    is java.sql.Date -> toLocalDate()
    is java.sql.Time -> toLocalTime()
    is java.sql.Timestamp -> toLocalDateTime()
    else -> this
}

fun KClass<*>.toSqlType(name: String, size: Int?): String = when (this) {
    in String::class -> "VARCHAR(${sizeRequired(name, size)})"
    in Byte::class -> "TINYINT"
    in Short::class -> "SMALLINT"
    in Int::class -> "INTEGER"
    in Long::class -> "BIGINT"
    in Float::class -> "FLOAT"
    in Double::class -> "DOUBLE"
    in BigDecimal::class -> "DECIMAL" // or "NUMERIC"
    in ByteArray::class -> "VARBINARY(${sizeRequired(name, size)})"
    in LocalDate::class -> "DATE"
    in LocalDateTime::class -> "TIMESTAMP"
    in LocalTime::class -> "TIME"
    in OffsetTime::class -> "TIME WITH TIMEZONE"
    in ZonedDateTime::class -> "TIMESTAMP WITH TIMEZONE"
    else -> throw IllegalArgumentException("Cannot determine external value of type $this")
}

fun KType.toSqlType(name: String): String = when (this.classifier as KClass<*>) {
    in String::class -> "VARCHAR(${sizeRequired(name, this.findAnnotation<Size>()?.value)})"
    in Byte::class -> "TINYINT"
    in Short::class -> "SMALLINT"
    in Int::class -> "INTEGER"
    in Long::class -> "BIGINT"
    in Float::class -> "FLOAT"
    in Double::class -> "DOUBLE"
    in BigDecimal::class -> "DECIMAL" // or "NUMERIC"
    in ByteArray::class -> "VARBINARY(${sizeRequired(name, this.findAnnotation<Size>()?.value)})"
    in LocalDate::class -> "DATE"
    in LocalDateTime::class -> "TIMESTAMP"
    in LocalTime::class -> "TIME"
    in OffsetTime::class -> "TIME WITH TIMEZONE"
    in ZonedDateTime::class -> "TIMESTAMP WITH TIMEZONE"
    else -> throw IllegalArgumentException("Cannot determine external value of type $this")
}

private fun sizeRequired(name: String, size: Int?): Int = size ?: throw IllegalArgumentException("Field «$name»: size is required!")
