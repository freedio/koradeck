/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.module

import com.coradec.coradeck.db.ctrl.SqlEngine
import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.model.RecordView
import com.coradec.coradeck.module.model.CoraModule
import kotlin.reflect.KClass

object CoraDB: CoraModule<CoraDBAPI>() {
    /** Opens a table (creating it if it doesn't exist) with the specified model in the default DB. */
    fun <Record: Any> Table(model: KClass<out Record>): RecordTable<Record> = impl.creopenTable(model)
    /** Creates a table with the specified model in the default DB. */
    fun <Record: Any> createTable(model: KClass<out Record>): RecordTable<Record> = impl.createTable(model)
    /** Opens a table with the specified model in the default DB. */
    fun <Record: Any> openTable(model: KClass<out Record>): RecordTable<Record> = impl.openTable(model)
    /** Opens a data view (unmodifiable table, creating it if it doesn't exist) with the specified model in the default DB. */
    fun <Record: Any> View(model: KClass<out Record>): RecordView<Record> = impl.creopenView(model)
    /** Creates a data view (unmodifiable table) with the specified model in the default DB. */
    fun <Record: Any> createView(model: KClass<out Record>): RecordView<Record> = impl.createView(model)
    /** Opens a data view (unmodifiable table) with the specified model in the default DB. */
    fun <Record: Any> openView(model: KClass<out Record>): RecordView<Record> = impl.openView(model)
    /** Returns an SQL engine for the specified model. */
    fun <Record: Any> engine(model: KClass<out Record>): SqlEngine<Record> = impl.engine(model)
}
