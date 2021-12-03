/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.com.model.impl.BasicVoucher
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.swallow
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.util.toSqlObjectName
import com.coradec.coradeck.db.util.toSqlValueRepr
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.view.View
import com.coradec.coradeck.text.model.LocalText
import kotlin.reflect.KClass

class HsqlDbTable<Record : Any>(db: Database, model: KClass<Record>) : HsqlDbCollection<Record>(db, model) {
    private fun plusAssign(element: Record) = insert(element).swallow()
    private fun plusAssign(elements: Iterable<Record>) = insert(elements).swallow()
    private fun plusAssign(elements: Sequence<Record>) = insert(elements).swallow()
    private fun minusAssign(selector: Selection) = delete(selector).swallow()

    private fun insert(element: Record): Int = accept(InsertRecordVoucher(here, element)).content.value
    private fun insert(elements: Iterable<Record>): Int = accept(InsertRecordsVoucher(here, elements.asSequence())).content.value
    private fun insert(elements: Sequence<Record>): Int = accept(InsertRecordsVoucher(here, elements)).content.value
    private fun update(selector: Selection, vararg fields: Pair<String, Any?>): Int =
        accept(UpdateRecordVoucher(here, selector, fields.asSequence())).content.value
    private fun delete(selector: Selection): Int = accept(DeleteRecordsVoucher(here, selector)).content.value

    override fun close() {
        commit()
        super.close()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <V : View> lookupView(session: Session, type: KClass<V>): V? = when(type) {
        in RecordTable::class -> InternalRecordTableView(session) as V
        else -> super.lookupView(session, type)
    }

    override fun onInitializing() {
        super.onInitializing()
        assertTable()
        route(InsertRecordVoucher::class, ::insertRecord)
        route(InsertRecordsVoucher::class, ::insertRecords)
        route(UpdateRecordVoucher::class, ::updateRecord)
        route(DeleteRecordsVoucher::class, ::deleteRecord)
    }

    override fun onFinalizing() {
        super.onFinalizing()
        close()
    }

    private fun assertTable() {
        val create = "create table if not exists $tableName (${columnDefinitions.entries.joinToString { "${it.key} ${it.value}" }})"
        try {
            debug("Executing SQL statement «%s».", create)
            statement.executeUpdate(create)
        } catch (e: Exception) {
            error(e, TEXT_TABLE_CREATION_FAILED, tableName)
            db.failed()
            throw e
        }
        columnDefinitions.forEach { (colName, colDef) ->
            if (colDef.indexed) {
                val index = "create index if not exists ${colName}_NDX on $tableName ($colName)"
                try {
                    debug("Executing SQL statement «%s».", index)
                    statement.executeUpdate(index)
                } catch (e: Exception) {
                    warn(e, TEXT_INDEXING_FAILED, tableName, colName)
                }
            }
        }
    }

    private fun discardTable() {
        val stmt = "drop table $tableName"
        try {
            statement.executeUpdate(stmt)
        } catch (e: Exception) {
            error(e, TEXT_TABLE_DROP_FAILED, tableName)
            db.failed()
            throw e
        }
    }

    private fun insertRecord(voucher: InsertRecordVoucher) {
        val element = voucher.element
        try {
            val record = element::class.members.filter { it.name in insertFieldNames }.associate { Pair(it.name, it.call(element)) }
            val stmt = "insert into %s (%s) values (%s)".format(
                tableName,
                record.keys.joinToString { it.toSqlObjectName() },
                record.values.joinToString { it.toSqlValueRepr() }
            )
            debug("Executing «$stmt»")
            voucher.value = statement.executeUpdate(stmt)
            voucher.succeed()
        } catch (e: Exception) {
            error(e, TEXT_INSERT_FAILED, tableName)
            db.failed()
            voucher.fail(e)
        }
    }

    private fun insertRecords(voucher: InsertRecordsVoucher) {
        val elements = voucher.elements
        val elementType = elements.singleOrNull()?.let { it::class } ?: Nothing::class
        var elementCount = 0
        try {
            elements.forEach { element ->
                val record = elementType.members.filter { it.name in fieldNames }.associate { Pair(it.name, it.call(element)) }
                val stmt = "insert into %s (%s) values (%s)".format(
                    tableName,
                    record.keys.joinToString { it.toSqlObjectName() },
                    record.values.joinToString { it.toSqlValueRepr() }
                )
                debug("Executing «$stmt»")
                elementCount += statement.executeUpdate(stmt)
            }
            voucher.value = elementCount
            voucher.succeed()
        } catch (e: Exception) {
            error(e, TEXT_INSERTS_FAILED, tableName)
            db.failed()
            voucher.fail(e)
        }
    }

    private fun updateRecord(voucher: UpdateRecordVoucher) {
        val selector = voucher.selector
        val fields = voucher.fields
        try {
            val stmt = "update %s set %s%s".format(
                tableName,
                fields.joinToString(",", "(", ")") { (name, value) ->
                    "${name.toSqlObjectName()} = ${value.toSqlValueRepr()}"
                },
                selector.filter
            )
            debug("Executing command «$stmt»")
            voucher.value = statement.executeUpdate(stmt)
            voucher.succeed()
        } catch (e: Exception) {
            error(e, TEXT_UPDATE_FAILED)
            db.failed()
            voucher.fail(e)
        }
    }

    private fun deleteRecord(voucher: DeleteRecordsVoucher) {
        val selector = voucher.selector
        val stmt = "delete from %s%s".format(tableName, selector.filter)
        try {
            debug("Executing command «$stmt»")
            voucher.value = statement.executeUpdate(stmt)
            voucher.succeed()
        } catch (e: Exception) {
            error(e, TEXT_DELETE_FAILED)
            db.failed()
            voucher.fail(e)
        }
    }

    class InsertRecordVoucher(origin: Origin, val element: Any): BasicVoucher<Int>(origin)
    class InsertRecordsVoucher(origin: Origin, val elements: Sequence<Any>): BasicVoucher<Int>(origin)
    class UpdateRecordVoucher(origin: Origin, val selector: Selection, val fields: Sequence<Pair<String, Any?>>):
        BasicVoucher<Int>(origin)
    class DeleteRecordsVoucher(origin: Origin, val selector: Selection): BasicVoucher<Int>(origin)

    private inner class InternalRecordTableView(session: Session) : InternalRecordCollectionView(session), RecordTable<Record> {
        override fun close() = this@HsqlDbTable.close()
        override fun insert(element: Record): Int = this@HsqlDbTable.insert(element)
        override fun insert(elements: Iterable<Record>): Int = this@HsqlDbTable.insert(elements)
        override fun insert(elements: Sequence<Record>): Int = this@HsqlDbTable.insert(elements)
        override fun update(selector: Selection, vararg fields: Pair<String, Any?>): Int = this@HsqlDbTable.update(selector, *fields)
        override fun delete(selector: Selection): Int = this@HsqlDbTable.delete(selector)
        override fun plusAssign(element: Record) = this@HsqlDbTable.plusAssign(element)
        override fun plusAssign(elements: Iterable<Record>) = this@HsqlDbTable.plusAssign(elements)
        override fun plusAssign(elements: Sequence<Record>) = this@HsqlDbTable.plusAssign(elements)
        override fun minusAssign(selector: Selection) = this@HsqlDbTable.minusAssign(selector)
        override fun commit() = this@HsqlDbTable.commit()
        override fun rollback() = this@HsqlDbTable.rollback()
        override fun standby() = this@HsqlDbTable.synchronize()
    }

    companion object {
        val TEXT_TABLE_CREATION_FAILED = LocalText("TableCreationFailed1")
        val TEXT_INDEXING_FAILED = LocalText("IndexingFailed2")
        val TEXT_TABLE_DROP_FAILED = LocalText("TableDropFailed1")
        val TEXT_INSERT_FAILED = LocalText("InsertFailed1")
        val TEXT_INSERTS_FAILED = LocalText("InsertsFailed1")
        val TEXT_UPDATE_FAILED = LocalText("UpdateFailed1")
        val TEXT_DELETE_FAILED = LocalText("DeleteFailed1")
    }
}
