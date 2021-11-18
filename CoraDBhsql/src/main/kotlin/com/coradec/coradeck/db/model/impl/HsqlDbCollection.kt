/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.bus.model.impl.BasicBusNode
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.model.ColumnDefinition
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordCollection
import com.coradec.coradeck.db.util.*
import com.coradec.module.db.annot.Generated
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class HsqlDbCollection<Record : Any>(
    protected val db: Database,
    private val model: KClass<out Record>
) : BasicBusNode(), RecordCollection<Record> {
    protected abstract val selector: Selection
    protected val connection = db.connection
    protected val statement = db.statement
    override val fieldNames: Sequence<String> get() = model.memberProperties.map { it.name }.asSequence()
    override val insertFieldNames: Sequence<String> get() =
        model.memberProperties
            .filter { p -> model.members.single { it.name == p.name }.returnType.findAnnotation<Generated>() == null }
            .map { it.name }.asSequence()
    override val tableName: String = model.simpleName?.toSqlObjectName()
        ?: throw IllegalArgumentException("Unsupported model: $model")
    override val columnNames: Sequence<String>
        get() = connection.metaData.getColumns(null, null, tableName, null)
            .seqOf(ColumnMetadata::class).map { it.columnName }.ifEmpty { fieldNames.map { it.toSqlObjectName() } }
    private val tableNames: Sequence<String>
        get() = connection.metaData
            .getTables(null, null, tableName, listOf("TABLE").toTypedArray())
            .seqOf(TableMetadata::class).map { it.tableName }
    override val fields: Map<String, KType>
        get() = model.members.filter { it.name in fieldNames }.associate { Pair(it.name, it.returnType) }
    override val columnDefinitions: Map<String, ColumnDefinition>
        get() = connection.metaData.getColumns(null, null, tableName, null)
            .listOf(ColumnMetadata::class)
            .map { Pair(it.columnName, BasicColumnDefinition(it.typeName.withSize(it.columnSize),it.nullable == 1)) }
            .ifEmpty {
                fields.map {
                    Pair(it.key.toSqlObjectName(), it.value.toColumnDef(it.key))
                }
            }
            .toMap()

    private fun count(selector: Selection): Int {
        val stmt = "select count(*) from $tableName${selector.filter}"
        debug("Executing query «$stmt»")
        return statement.executeQuery(stmt).checkedSingleOf(Int::class)
    }

    fun Pair<KClass<*>, List<Annotation>>.toSqlType(): String = first.toSqlType(second)

    override val size: Int get() = count(selector)
    override val all: Sequence<Record> get() = select(selector)
    override fun close() = db.close()
    fun commit() = connection.commit()
    fun rollback() = connection.rollback()
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
