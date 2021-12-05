/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.bus.model.impl.BasicBusHub
import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.module.CoraControl
import com.coradec.coradeck.db.com.CreateTableVoucher
import com.coradec.coradeck.db.com.GetTableVoucher
import com.coradec.coradeck.db.com.OpenTableVoucher
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.util.seqOf
import com.coradec.coradeck.db.util.toSqlTableName
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.view.impl.BasicView
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.type.model.Password
import java.net.URI
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class HsqlDatabase(
    private val uri: URI,
    private val username: String,
    private val password: Password,
    private val autocommit: Boolean = false
) : BasicBusHub() {
    val databaseView: Database = InternalDatabaseView(Session.current)
    private var myConnection: Connection? = null
    private val connection: Connection get() = myConnection ?: throw IllegalStateException("Database «%s» not attached!")
    private val statement: Statement get() = connection.createStatement()
    private var failed: Boolean = false

    override fun onInitializing() {
        super.onInitializing()
        myConnection = DriverManager.getConnection(uri.toASCIIString(), username, password.decoded)
        route(GetTableVoucher::class, ::getTable)
        route(OpenTableVoucher::class, ::openTable)
        route(CreateTableVoucher::class, ::createTable)
    }

    override fun onInitialized() {
        super.onInitialized()
        debug("Database ‹%s› initialized.", uri)
        statement.executeUpdate("set autocommit $autocommit")
    }

    override fun onFinalizing() {
        super.onFinalizing()
        if (failed) connection.rollback() else connection.commit()
        connection.close()
    }

    private fun close() {
        detach()
    }

    private fun failed() {
        failed = true
    }

    private fun <Record : Any> getTable(model: KClass<out Record>): RecordTable<Record> {
        val modelClass = model.classname
        debug("GetTable order for record ‹%s›.", modelClass)
        return accept(GetTableVoucher(here, model)).content.value.also { debug("GetTable ‹%s› order filled", modelClass) }
    }

    private fun <Record : Any> openTable(model: KClass<out Record>): RecordTable<Record> {
        val modelClass = model.classname
        debug("OpenTable order for record ‹%s›.", modelClass)
        return accept(OpenTableVoucher(here, model)).content.value.also { debug("OpenTable ‹%s› order filled", modelClass) }
    }

    private fun reset() {
        val tableTypes = listOf("TABLE", "VIEW")
        names.value.forEach { member -> remove(member) }
        connection.metaData.getTables(null, null, null, null)
            .seqOf(TableMetadata::class)
            .filter { it.tableType in tableTypes }
            .map { it.tableName }
            .forEach { tableName ->
                info(TEXT_DROPPING_TABLE, tableName)
                statement.executeUpdate("drop table $tableName")
            }
    }

    private fun getTable(voucher: GetTableVoucher<*>) {
        lookup(voucher.model.toSqlTableName()).forwardAs(voucher as Voucher<RecordTable<*>>) { member, session ->
            member.getView(session, RecordTable::class)
        }
    }

    private fun openTable(voucher: OpenTableVoucher<*>) {
        val model = voucher.model
        debug("Opening table ‹%s›...", model.classname)
        lookup(model.toSqlTableName()).whenVoucherFinished {
            debug("OpenTable ‹%s›: lookup finished with state ‹%s›.", model.classname, state)
            when (state) {
                FAILED -> accept(CreateTableVoucher(here, model)).content.apply {
                    forwardTo(voucher as Voucher<Any?>)
                }
                else -> {
                    (voucher as Voucher<Any?>).value = value.getView(voucher.session, RecordTable::class)
                    voucher.succeed()
                }
            }
        }
    }

    private fun createTable(voucher: CreateTableVoucher<*>) {
        val name = voucher.model.toSqlTableName()
        val node = HsqlDbTable(InternalDatabaseView(Session.current), voucher.model)
        val memberView = node.memberView
        debug("Creating table ‹%s›", name)
        add(name, memberView).whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    (voucher as Voucher<RecordTable<*>>).value =
                        memberView.getView(voucher.session, RecordTable::class)
                    voucher.succeed()
                }
                FAILED -> voucher.fail(reason)
                CANCELLED -> voucher.cancel(reason)
                else -> relax()
            }
        }
    }

    private inner class InternalDatabaseView(session: Session) : BasicView(session), Database {
        override val connection: Connection get() = this@HsqlDatabase.connection
        override val statement: Statement get() = this@HsqlDatabase.statement
        override val memberView: MemberView = this@HsqlDatabase.memberView
        override fun <Record : Any> getTable(model: KClass<out Record>): RecordTable<Record> = this@HsqlDatabase.getTable(model)
        override fun <Record : Any> openTable(model: KClass<out Record>): RecordTable<Record> = this@HsqlDatabase.openTable(model)
        override fun reset() = this@HsqlDatabase.reset()
        override fun close() = this@HsqlDatabase.close()
        override fun failed() = this@HsqlDatabase.failed()
    }

    companion object {
        private val IMMEX = CoraControl.IMMEX
        private val TEXT_DROPPING_TABLE = LocalText("DroppingTable1")
    }
}
