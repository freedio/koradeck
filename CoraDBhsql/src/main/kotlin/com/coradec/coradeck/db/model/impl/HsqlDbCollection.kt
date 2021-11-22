/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.bus.model.impl.BasicBusNode
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.db.annot.Generated
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.ctrl.impl.SqlSelection
import com.coradec.coradeck.db.model.ColumnDefinition
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordCollection
import com.coradec.coradeck.db.util.*
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.view.View
import com.coradec.coradeck.session.view.impl.BasicView
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class HsqlDbCollection<Record : Any>(
    protected val db: Database,
    private val model: KClass<out Record>
) : BasicBusNode() {
    private val recordName: String = model.classname
    private val selector: Selection get() = SqlSelection.ALL
    private val connection = db.connection
    protected val statement = db.statement
    protected val fieldNames: Sequence<String> get() = model.memberProperties.map { it.name }.asSequence()
    protected val insertFieldNames: Sequence<String> get() =
        model.memberProperties
            .filter { p -> model.members.single { it.name == p.name }.returnType.findAnnotation<Generated>() == null }
            .map { it.name }.asSequence()
    protected val tableName: String = model.simpleName?.toSqlObjectName()
        ?: throw IllegalArgumentException("Unsupported model: $model")
    private val columnNames: Sequence<String>
        get() = connection.metaData.getColumns(null, null, tableName, null)
            .seqOf(ColumnMetadata::class).map { it.columnName }.ifEmpty { fieldNames.map { it.toSqlObjectName() } }
    private val tableNames: Sequence<String>
        get() = connection.metaData
            .getTables(null, null, tableName, listOf("TABLE").toTypedArray())
            .seqOf(TableMetadata::class).map { it.tableName }
    private val fields: Map<String, KType>
        get() = model.members.filter { it.name in fieldNames }.associate { Pair(it.name, it.returnType) }
    protected val columnDefinitions: Map<String, ColumnDefinition>
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

    private val size: Int get() = count(selector)
    private val all: Sequence<Record> get() = select(selector)
    protected open fun close() = db.close()
    fun commit() = connection.commit()
    fun rollback() = connection.rollback()
    private fun select(selector: Selection): Sequence<Record> {
        try {
            val stmt = "select * from $tableName${selector.select}"
            debug("Executing query «$stmt»")
            return statement.executeQuery(stmt).asSequence(model)
        } catch (e: Exception) {
            error(e)
            throw e
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <V : View> lookupView(session: Session, type: KClass<V>): V? = when(type) {
        in RecordCollection::class -> InternalRecordCollectionView(session) as V
        else -> super.lookupView(session, type)
    }

    protected open inner class InternalRecordCollectionView(session: Session): BasicView(session), RecordCollection<Record> {
        override val recordName: String get() = this@HsqlDbCollection.recordName
        override val fieldNames: Sequence<String> get() = this@HsqlDbCollection.fieldNames
        override val insertFieldNames: Sequence<String> = this@HsqlDbCollection.insertFieldNames
        override val tableName: String get() = this@HsqlDbCollection.tableName
        override val columnNames: Sequence<String> get() = this@HsqlDbCollection.columnNames
        override val fields: Map<String, KType> get() = this@HsqlDbCollection.fields
        override val columnDefinitions: Map<String, ColumnDefinition> get() = this@HsqlDbCollection.columnDefinitions
        override val size: Int get() = this@HsqlDbCollection.size
        override val all: Sequence<Record> get() = this@HsqlDbCollection.all

        override fun select(selector: Selection): Sequence<Record> = this@HsqlDbCollection.select(selector)
        override fun close() = this@HsqlDbCollection.close()
        override fun iterator(): Iterator<Record> = select(selector).iterator()
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
