/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.module

import com.coradec.coradeck.db.ctrl.SqlEngine
import com.coradec.coradeck.db.ctrl.impl.HsqlEngine
import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.model.RecordView
import com.coradec.coradeck.db.model.impl.HsqlDbTable
import com.coradec.coradeck.db.model.impl.HsqlDbView
import kotlin.reflect.KClass

class CoraDbHsql: CoraDBAPI {
    /** Opens a table (creating it if it doesn't exist) with the specified model in the default DB. */
    override fun <Record: Any> creopenTable(model: KClass<out Record>): RecordTable<Record> =  HsqlDbTable.creopen(model)
    /** Creates a table with the specified model in the default DB. */
    override fun <Record: Any> createTable(model: KClass<out Record>): RecordTable<Record> = HsqlDbTable.create(model)
    /** Opens a table with the specified model in the default DB. */
    override fun <Record: Any> openTable(model: KClass<out Record>): RecordTable<Record> = HsqlDbTable.open(model)
    /** Opens a data view (unmodifiable table, creating it if it doesn't exist) with the specified model in the default DB. */
    override fun <Record: Any> creopenView(model: KClass<out Record>): RecordView<Record> = HsqlDbView.creopen(model)
    /** Creates a data view (unmodifiable table) with the specified model in the default DB. */
    override fun <Record: Any> createView(model: KClass<out Record>): RecordView<Record> = HsqlDbView.create(model)
    /** Opens a data view (unmodifiable table) with the specified model in the default DB. */
    override fun <Record: Any> openView(model: KClass<out Record>): RecordView<Record> = HsqlDbView.open(model)
    /** Returns an SQL engine for the specified model. */
    override fun <Record: Any> engine(model: KClass<out Record>): SqlEngine<Record> = HsqlEngine(model)
}
