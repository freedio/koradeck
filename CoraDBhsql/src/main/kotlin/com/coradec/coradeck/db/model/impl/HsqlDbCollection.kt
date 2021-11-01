/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.bus.model.impl.BasicBusNode
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordCollection
import com.coradec.coradeck.db.util.*
import com.coradec.module.db.annot.Size
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties

abstract class HsqlDbCollection<Record : Any>(
    private val db: Database,
    private val model: KClass<out Record>
) : BasicBusNode(), RecordCollection<Record> {
    protected abstract val selector: Selection
    protected val connection = db.connection
    protected val statement = db.statement
    override val fieldNames: Sequence<String> get() = model.memberProperties.map { it.name }.asSequence()
    override val tableName: String = model.simpleName?.toSqlObjectName()
        ?: throw IllegalArgumentException("Unsupported model: $model")
    override val columnNames: Sequence<String>
        get() = connection.metaData.getColumns(null, null, tableName, null)
            .seqOf(ColumnMetadata::class).map { it.columnName }.ifEmpty {
                fieldNames.map { it.toSqlObjectName() }
            }
    private val tableNames: Sequence<String>
        get() = connection.metaData
            .getTables(null, null, tableName, listOf("TABLE").toTypedArray())
            .seqOf(TableMetadata::class).map { it.tableName }
    override val fields: Map<String, KType>
        get() = model.members.filter { it.name in fieldNames }.associate { Pair(it.name, it.returnType) }
    override val columnDefinitions: Map<String, String>
        get() = connection.metaData
            .getColumns(null, null, tableName, null)
            .listOf(ColumnMetadata::class).map { Pair(it.columnName, it.typeName.withSize(it.columnSize)) }.ifEmpty {
                fields.map { Pair(it.key.toSqlObjectName(), it.value.toSqlType(it.key)) }
            }.toMap()

    private fun count(selector: Selection): Int {
        val stmt = "select count(*) from $tableName${selector.filter}"
        debug("Executing query «$stmt»")
        return statement.executeQuery(stmt).checkedSingleOf(Int::class)
    }

    private fun String.withSize(columnSize: Int?): String = if (columnSize == null) this else when (this) {
        "VARCHAR", "CHAR", "LONGVARCHAR" -> "$this($columnSize)"
        "VARBINARY", "BINARY" -> "#this($columnSize)"
        else -> this
    }

    protected fun Any?.toSqlValueRepr() = when (this) {
        null -> "NULL"
        is String -> "'$this'"
        is LocalDate -> "DATE '$this'"
        is LocalTime -> "TIME '$this'"
        is LocalDateTime -> "TIMESTAMP '${this.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}'"
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

    override fun onInitializing() {
        super.onInitializing()
    }

    override val size: Int get() = count(selector)
    override val all: Sequence<Record> get() = select(selector)
    override fun close() = db.close()
    fun commit() = connection.commit()
    override fun select(selector: Selection): Sequence<Record> {
        val stmt = "select * from $tableName${selector.select}"
        debug("Executing query «$stmt»")
        @Suppress("UNCHECKED_CAST")
        return statement.executeQuery(stmt).asSequence(model)
    }
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
