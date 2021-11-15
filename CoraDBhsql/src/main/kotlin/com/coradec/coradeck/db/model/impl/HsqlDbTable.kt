/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.core.util.classname
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

    override fun insert(element: Record): Int {
        try {
            val record = element::class.members.filter { it.name in fieldNames }.associate { Pair(it.name, it.call(element)) }
            val stmt = "insert into %s (%s) values (%s)".format(
                tableName,
                record.keys.joinToString { it.toSqlObjectName() },
                record.values.joinToString { it.toSqlValueRepr() }
            )
            debug("Executing command «$stmt»")
            return statement.executeUpdate(stmt)
        } catch (e: Exception) {
            error(e)
            throw e
        }
    }

    override fun insert(elements: Iterable<Record>): Int = elements.sumOf { insert(it) }
    override fun insert(elements: Sequence<Record>): Int = elements.sumOf { insert(it) }
    override fun update(selector: Selection, vararg fields: Pair<String, Any?>): Int {
        val stmt = "update %s set %s%s".format(
            tableName,
            fields.joinToString(",", "(", ")") { (name, value) ->
                "${name.toSqlObjectName()} = ${value.toSqlValueRepr()}"
            },
            selector.filter
        )
        debug("Executing command «$stmt»")
        return statement.executeUpdate(stmt)
    }

    override fun delete(selector: Selection): Int {
        val stmt = "delete from %s%s".format(tableName, selector.filter)
        debug("Executing command «$stmt»")
        return statement.executeUpdate(stmt)
    }

    override fun close() {
        commit()
        super.close()
    }

    override fun onInitializing() {
        super.onInitializing()
        assertTable()
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

    override fun iterator(): Iterator<Record> = select(selector).iterator()
}
