/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.ctrl.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.ctrl.SqlEngine
import com.coradec.coradeck.db.util.asSequence
import com.coradec.coradeck.db.util.checkedSingleOf
import com.coradec.coradeck.db.util.listOf
import com.coradec.coradeck.db.util.toSqlObjectName
import com.coradec.module.db.annot.Size
import java.math.BigDecimal
import java.sql.DriverManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

open class HsqlEngine<T : Any>(private val model: KClass<out T>) : Logger(), SqlEngine<T>, AutoCloseable {
    private val connection = DriverManager.getConnection(dbUrlProperty.value, usernameProperty.value, passwordProperty.value)
    override val tableName: String = model.simpleName?.toSqlObjectName()
        ?: throw IllegalArgumentException("Unsupported model: $model")
    internal val columnNames: List<String>
        get() = connection.metaData
            .getColumns(null, null, tableName, null)
            .listOf(ColumnMetadata::class).map { it.columnName }.ifEmpty {
                fieldNames.map { it.toSqlObjectName() }
            }
    internal val tableNames: List<String>
        get() = connection.metaData
            .getTables(null, null, tableName, listOf("TABLE").toTypedArray())
            .listOf(TableMetadata::class).map { it.tableName }
    internal val columnDefinitions: List<Pair<String, String>>
        get() = connection.metaData
            .getColumns(null, null, tableName, null)
            .listOf(ColumnMetadata::class).map { Pair(it.columnName, it.typeName.withSize(it.columnSize)) }.ifEmpty {
                fields.map { Pair(it.key.toSqlObjectName(), it.value.toSqlType()) }
            }
    internal val fields: Map<String, Pair<KClass<*>, List<Annotation>>>
        get() = model.members
            .filter { it.name in fieldNames }
            .associate { Pair(it.name, Pair(it.returnType.classifier as KClass<*>, it.returnType.annotations)) }
    internal val fieldNames: List<String> get() = model.memberProperties.map { it.name }
    private val statement = connection.createStatement()

    override fun count(selector: Selection): Int {
        val stmt = "select count(*) from $tableName${selector.filter}"
        debug("Executing query «$stmt»")
        return statement.executeQuery(stmt).checkedSingleOf(Int::class)
    }

    override fun query(selector: Selection): Sequence<T> {
        val stmt = "select * from $tableName${selector.select}"
        debug("Executing query «$stmt»")
        @Suppress("UNCHECKED_CAST")
        return statement.executeQuery(stmt).asSequence(model)
    }

    override fun insert(entry: T): Int {
        val record = entry::class.members.filter { it.name in fieldNames }.map { Pair(it.name, it.call(entry)) }.toMap()
        val stmt = "insert into $tableName (${record.keys.joinToString { it.toSqlObjectName() }}) " +
                "values (${record.values.joinToString { it.toSqlValueRepr() }})"
        debug("Executing command «$stmt»")
        return statement.executeUpdate(stmt)
    }

    override fun delete(selector: Selection): Int {
        @Suppress("SqlWithoutWhere") val stmt = "delete from $tableName${selector.filter}"
        debug("Executing command «$stmt»")
        return statement.executeUpdate(stmt)
    }

    override fun assertTable() {
        val stmt = "create table if not exists $tableName (${columnDefinitions.joinToString { "${it.first} ${it.second}" }})"
        debug("Executing command «$stmt»")
        statement.executeUpdate(stmt)
        connection.commit()
    }

    companion object {
        val dbUrlProperty = LocalProperty<String>("DbUrl")
        val usernameProperty = LocalProperty<String>("Username")
        val passwordProperty = LocalProperty<String>("Password")
    }

    override fun close() {
        commit()
        connection.close()
    }

    private fun String.withSize(columnSize: Int?): String = if (columnSize == null) this else when (this) {
        "VARCHAR", "CHAR", "LONGVARCHAR" -> "$this($columnSize)"
        "VARBINARY", "BINARY" -> "#this($columnSize)"
        else -> this
    }

    override fun commit() {
        connection.commit()
    }
}

private fun Any?.toSqlValueRepr() = when (this) {
    null -> "NULL"
    is String -> "'$this'"
    is LocalDate -> "DATE '$this'"
    is LocalTime -> "TIME '$this'"
    is LocalDateTime -> "TIMESTAMP '${this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}'"
    else -> toString()
}

private fun Pair<KClass<*>, List<Annotation>>.toSqlType(): String = first.toSqlType(second)
private fun KClass<*>.toSqlType(annotations: List<Annotation>): String = when (this) {
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
    else -> throw IllegalArgumentException("Type \"$this\" cannot be translated to SQL!")
}

data class TableMetadata(
    val tableCat: String?,
    val tableSchem: String?,
    val tableName: String,
    val tableType: String,
    val remarks: String?,
    val typeCat: String?,
    val typeSchem: String?,
    val typeName: String?,
    val selfReferencingColumnName: String?,
    val refGeneration: String?
)

data class ColumnMetadata(
    val tableCat: String?,
    val tableSchem: String?,
    val tableName: String,
    val columnName: String,
    val dataType: Int,
    val typeName: String,
    val columnSize: Int?,
    val decimalDigits: Long?,
    val numPrecRadix: Int?,
    val nullable: Int,
    val remarks: String?,
    val columnDef: String?,
    val charOctetLength: Int,
    val ordinalPosition: Int,
    val isNullable: String,
    val scopeCatalog: String?,
    val scopeSchema: String?,
    val scopeTable: String?,
    val sourceDataType: Short?,
    val isAutoIncrement: String?,
    val isGeneratedColumn: String?
)
