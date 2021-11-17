/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.com.model.impl.BasicVoucher
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.swallow
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.ctrl.impl.SqlSelection
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.util.toSqlObjectName
import com.coradec.coradeck.db.util.toSqlValueRepr
import kotlin.reflect.KClass

class HsqlDbTable<Record : Any>(db: Database, model: KClass<Record>) : HsqlDbCollection<Record>(db, model),
    RecordTable<Record> {
    override val selector = SqlSelection.ALL
    override val recordName: String = model.classname

    override fun plusAssign(element: Record) = insert(element).swallow()
    override fun plusAssign(elements: Iterable<Record>) = insert(elements).swallow()
    override fun plusAssign(elements: Sequence<Record>) = insert(elements).swallow()
    override fun minusAssign(selector: Selection) = delete(selector).swallow()

    override fun insert(element: Record): Int = accept(InsertRecordVoucher(here, element)).content.value
    override fun insert(elements: Iterable<Record>): Int = accept(InsertRecordsVoucher(here, elements.asSequence())).content.value
    override fun insert(elements: Sequence<Record>): Int = accept(InsertRecordsVoucher(here, elements)).content.value
    override fun update(selector: Selection, vararg fields: Pair<String, Any?>): Int =
        accept(UpdateRecordVoucher(here, selector, fields.asSequence())).content.value
    override fun delete(selector: Selection): Int = accept(DeleteRecordsVoucher(here, selector)).content.value

    override fun close() {
        commit()
        super.close()
    }

    override fun onInitializing() {
        super.onInitializing()
        assertTable()
        route(InsertRecordVoucher::class, ::insertRecord)
        route(InsertRecordsVoucher::class, ::insertRecords)
        route(UpdateRecordVoucher::class, ::updateRecord)
        route(DeleteRecordsVoucher::class, ::deleteRecord)
    }

    private fun assertTable() {
        val stmt = "create table if not exists $tableName (${columnDefinitions.entries.joinToString { "${it.key} ${it.value}" }})"
        debug("Executing command «$stmt»")
        statement.executeUpdate(stmt)
        connection.commit()
    }

    private fun discardTable() {
        val stmt = "drop table $tableName"
        statement.executeUpdate(stmt)
        connection.commit()
    }

    private fun insertRecord(voucher: InsertRecordVoucher) {
        val element = voucher.element
        try {
            val record = element::class.members.filter { it.name in fieldNames }.associate { Pair(it.name, it.call(element)) }
            val stmt = "insert into %s (%s) values (%s)".format(
                tableName,
                record.keys.joinToString { it.toSqlObjectName() },
                record.values.joinToString { it.toSqlValueRepr() }
            )
            debug("Executing «$stmt»")
            voucher.value = statement.executeUpdate(stmt)
            voucher.succeed()
        } catch (e: Exception) {
            error(e)
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
            error(e)
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
            error(e)
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
            error(e)
            voucher.fail(e)
        }
    }

    override fun iterator(): Iterator<Record> = select(selector).iterator()

    class InsertRecordVoucher(origin: Origin, val element: Any): BasicVoucher<Int>(origin)
    class InsertRecordsVoucher(origin: Origin, val elements: Sequence<Any>): BasicVoucher<Int>(origin)
    class UpdateRecordVoucher(origin: Origin, val selector: Selection, val fields: Sequence<Pair<String, Any?>>):
        BasicVoucher<Int>(origin)
    class DeleteRecordsVoucher(origin: Origin, val selector: Selection): BasicVoucher<Int>(origin)
}
