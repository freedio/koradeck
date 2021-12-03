/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.db.ctrl.Selection

interface RecordTable<Record: Any>: RecordCollection<Record> {
    /** Adds the specified element to the table. */
    operator fun plusAssign(element: Record)
    /** Adds the specified elements to the table. */
    operator fun plusAssign(elements: Iterable<Record>)
    /** Adds the specified elements to the table. */
    operator fun plusAssign(elements: Sequence<Record>)
    /** Removes the elements satisfying the specified selector from the table. */
    operator fun minusAssign(selector: Selection)
    /** Adds the specified element to the table. */
    fun insert(element: Record): Int
    /** Adds the specified elements to the table. */
    fun insert(elements: Iterable<Record>): Int
    /** Adds the specified elements to the table. */
    fun insert(elements: Sequence<Record>): Int
    /** Removes the elements satisfying the specified selector from the table. */
    fun delete(selector: Selection): Int
    /** Updates the specified fields in the element satisfying the specified selector. */
    fun update(selector: Selection, vararg fields: Pair<String, Any?>): Int
    /** Commits the changes made so far. */
    fun commit()
    /** Cancels the chanches made so far. */
    fun rollback()
    /** Waits until all pending operations have completed. */
    fun standby()
}
