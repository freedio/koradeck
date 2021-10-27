/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.module

import com.coradec.coradeck.db.ctrl.SqlEngine
import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.model.RecordView
import com.coradec.coradeck.module.model.CoraModuleAPI
import kotlin.reflect.KClass

interface CoraDBAPI: CoraModuleAPI {
    /** Opens a table (creating it if it doesn't exist) with the specified model in the default DB. */
    fun <Record: Any> creopenTable(model: KClass<out Record>): RecordTable<Record>
    /** Creates a table with the specified model in the default DB. */
    fun <T: Any> createTable(model: KClass<out T>): RecordTable<T>
    /** Opens a table with the specified model in the default DB. */
    fun <T: Any> openTable(model: KClass<out T>): RecordTable<T>
    /** Opens a data view (unmodifiable table, creating it if it doesn't exist) with the specified model in the default DB. */
    fun <Record: Any> creopenView(model: KClass<out Record>): RecordView<Record>
    /** Creates a data view (unmodifiable table) with the specified model in the default DB. */
    fun <T: Any> createView(model: KClass<out T>): RecordView<T>
    /** Opens a data view (unmodifiable table) with the specified model in the default DB. */
    fun <T: Any> openView(model: KClass<out T>): RecordView<T>
    /** Returns an SQL engine for the specified model. */
    fun <Record: Any> engine(model: KClass<out Record>): SqlEngine<Record>
}
