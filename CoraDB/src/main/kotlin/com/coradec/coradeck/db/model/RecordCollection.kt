/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.session.view.View
import kotlin.reflect.KType

interface RecordCollection<Record: Any>: View, Iterable<Record>, AutoCloseable {
    /** Map of field types by name. */
    val fields: Map<String, KType>
    /** Name of the record in the collection (corresponds to the record's class name). */
    val recordName: String
    /** SQL name of the collection, also called "table name", even for views and other collections. */
    val tableName: String
    /** A sequence of field names in the collection. */
    val fieldNames: Sequence<String>
    /** A sequence of field names for insertion ([fieldNames] without generated fields). */
    val insertFieldNames: Sequence<String>
    /** A set of SQL column names in the collection. */
    val columnNames: Sequence<String>
    /** A map of SQL column definitions in the collection by field name. */
    val columnDefinitions: Map<String, ColumnDefinition>
    /** A sequence of all the records in the collection.  The sequence can be traversed only once. */
    val all: Sequence<Record>
    /** Number of entries in the collection. */
    val size: Int
    /** Lock/unlock the table for reading (by setting to true/false). */
    var readLock: Boolean
    /** Lock/unlock the table for writing (by setting to true/false). */
    var writeLock: Boolean


    /** A sequence of the records in this collection limited to the specified selector. */
    fun select(selector: Selection): Sequence<Record>
}
