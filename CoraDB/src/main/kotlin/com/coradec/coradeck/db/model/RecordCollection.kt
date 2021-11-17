/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.db.ctrl.Selection
import kotlin.reflect.KType

interface RecordCollection<R: Any>: BusNode, Iterable<R>, AutoCloseable {
    /** Map of field types by name. */
    val fields: Map<String, KType>
    /** Name of the record in the collection (corresponds to the record's class name). */
    val recordName: String
    /** SQL name of the collection, also called "table name", even for views and other collections. */
    val tableName: String
    /** A set of field names in the collection. */
    val fieldNames: Sequence<String>
    /** A set of SQL column names in the collection. */
    val columnNames: Sequence<String>
    /** A map of SQL column definitions in the collection by field name. */
    val columnDefinitions: Map<String, ColumnDefinition>
    /** A sequence of all the records in the collection.  The sequence can be traversed only once. */
    val all: Sequence<R>
    /** Number of entries in the collection. */
    val size: Int

    /** A sequence of the records in this collection limited to the specified selector. */
    fun select(selector: Selection): Sequence<R>
}
