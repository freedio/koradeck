/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.bus.model.impl.BasicBusHub
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.Voucher
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.db.com.CreateTableVoucher
import com.coradec.coradeck.db.com.GetTableVoucher
import com.coradec.coradeck.db.com.OpenTableVoucher
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.util.toSqlTableName
import com.coradec.coradeck.type.model.Password
import java.net.URI
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class HsqlDatabase(private val uri: URI, private val username: String, private val password: Password): BasicBusHub(), Database {
    var myConnection: Connection? = null
    override val connection: Connection get() = myConnection ?: throw IllegalStateException("Database «%s» not attached!")
    override val statement: Statement get() = connection.createStatement()

    override fun onInitializing() {
        super.onInitializing()
        myConnection = DriverManager.getConnection(uri.toASCIIString(), username, password.decoded)
        route(GetTableVoucher::class, ::getTable)
        route(OpenTableVoucher::class, ::openTable)
        route(CreateTableVoucher::class, ::createTable)
    }

    override fun onInitialized() {
        super.onInitialized()
        debug("Database ‹%s› initialized.")
    }

    override fun onFinalizing() {
        super.onFinalizing()
        unroute(GetTableVoucher::class)
        connection.close()
    }

    override fun close() {
        leave()
    }

    override fun <Record : Any> getTable(model: KClass<out Record>): RecordTable<Record> =
        accept(GetTableVoucher(here, model)).content.value

    override fun <Record : Any> openTable(model: KClass<out Record>): RecordTable<Record> =
        accept(OpenTableVoucher(here, model)).content.value

    private fun getTable(voucher: GetTableVoucher<*>) {
        lookup(voucher.model.toSqlTableName()).forwardTo(voucher as Voucher<BusNode>)
    }

    private fun openTable(voucher: OpenTableVoucher<*>) {
        val model = voucher.model
        debug("Opening table ‹%s›...", model.classname)
        lookup(model.toSqlTableName()).whenVoucherFinished {
            when (state) {
                FAILED -> CreateTableVoucher(here, model).also { accept(it) } as Voucher<BusNode>
                else -> this
            }.forwardTo(voucher as Voucher<BusNode>)
        }
    }

    private fun createTable(voucher: CreateTableVoucher<*>) {
        val name = voucher.model.toSqlTableName()
        val node = HsqlDbTable(this, voucher.model)
        debug("Creating table ‹%s›", name)
        add(name, node).whenFinished {
            when (state) {
                SUCCESSFUL -> {
                    (voucher as Voucher<BusNode>).value = node
                    voucher.succeed()
                }
                FAILED -> voucher.fail(reason)
                CANCELLED -> voucher.cancel(reason)
                else -> relax()
            }
        }
    }
}
