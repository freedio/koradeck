/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.util

import com.coradec.coradeck.core.model.SqlTransformable
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.type.module.CoraType
import com.coradec.module.db.annot.Size
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
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
fun Any?.toSqlFieldValue(type: KType): Any? = when (this) {
    is java.sql.Date -> toLocalDate()
    is java.sql.Time -> toLocalTime()
    is java.sql.Timestamp -> toLocalDateTime()
    is OffsetDateTime -> ZonedDateTime.from(this)
    else -> when (type) {
        in SqlTransformable::class -> CoraType.castTo(this, type)
        else -> this
    }
}

@Suppress("UNCHECKED_CAST")
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
    in OffsetTime::class -> "TIME WITH TIME ZONE"
    in ZonedDateTime::class -> "TIMESTAMP WITH TIME ZONE"
    in SqlTransformable::class -> (companionObject?.memberProperties
        ?.singleOrNull { it.name == "sqlType" } as? KProperty1<Any, String>)
        ?.get(companionObjectInstance ?: throw IllegalStateException("SqlTransformable without companion object!"))
        ?: throw IllegalStateException("SqlTransformable without companion object or companion property ‹sqlType›!")
    else -> throw IllegalArgumentException("Cannot determine external value of type $this")
}

@Suppress("UNCHECKED_CAST")
fun KType.toSqlType(name: String): String = when (val klass = this.classifier as KClass<*>) {
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
    in OffsetTime::class -> "TIME WITH TIME ZONE"
    in ZonedDateTime::class -> "TIMESTAMP WITH TIME ZONE"
    in SqlTransformable::class -> (klass.companionObject?.memberProperties
        ?.singleOrNull { it.name == "sqlType" } as? KProperty1<Any, String>)
        ?.get(klass.companionObjectInstance ?: throw IllegalStateException("SqlTransformable without companion object!"))
        ?: throw IllegalStateException("SqlTransformable without companion object or companion property ‹sqlType›!")
    else -> throw IllegalArgumentException("Cannot determine external value of type $this")
}

private fun sizeRequired(name: String, size: Int?): Int = size ?: throw IllegalArgumentException("Field «$name»: size is required!")

fun String.withSize(columnSize: Int?): String = if (columnSize == null) this else when (this) {
    "VARCHAR", "CHAR", "LONGVARCHAR" -> "$this($columnSize)"
    "VARBINARY", "BINARY" -> "#this($columnSize)"
    else -> this
}

fun Any?.toSqlValueRepr() = when (this) {
    null -> "NULL"
    is String -> "'$this'"
    is LocalDate -> "DATE '$this'"
    is LocalTime -> "TIME '$this'"
    is LocalDateTime -> "TIMESTAMP '${this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}'"
    is ZonedDateTime -> "TIMESTAMP '${this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"))}'"
    is OffsetTime -> "TIMESTAMP '${this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"))}'"
    is SqlTransformable -> toSqlValue()
    else -> toString()
}

@Suppress("UNCHECKED_CAST")
fun KClass<*>.toSqlType(annotations: List<Annotation>): String = when (this) {
    String::class -> "VARCHAR(%d)".format((annotations.singleOrNull { it is Size } as? Size)?.value
        ?: throw IllegalArgumentException("Missing String @Size for ${this.java.name}"))
    Boolean::class -> "BIT"
    Byte::class -> "TINYINT"
    Short::class -> "SMALLINT"
    Int::class -> "INTEGER"
    Long::class -> "BIGINT"
    Float::class -> "FLOAT"
    Double::class -> "DOUBLE"
    BigDecimal::class -> "NUMERIC"
    LocalDate::class -> "DATE"
    LocalTime::class -> "TIME"
    LocalDateTime::class -> "TIMESTAMP"
    in SqlTransformable::class -> (companionObject?.memberProperties
        ?.singleOrNull { it.name == "sqlType" } as? KProperty1<Any, String>)
        ?.get(companionObjectInstance ?: throw IllegalStateException("SqlTransformable without companion object!"))
        ?: throw IllegalStateException("SqlTransformable without companion object or companion property ‹sqlType›!")
    else -> throw IllegalArgumentException("Type \"$this\" cannot be translated to SQL!")
}
