/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.db.ctrl.Selection

interface RecordTable<Record: Any>: RecordCollection<Record> {
    operator fun plusAssign(element: Record)
    operator fun plusAssign(elements: Iterable<Record>)
    operator fun plusAssign(elements: Sequence<Record>)
    operator fun minusAssign(selector: Selection)
    fun insert(element: Record): Int
    fun insert(elements: Iterable<Record>): Int
    fun insert(elements: Sequence<Record>): Int
    fun delete(selector: Selection): Int
    fun update(selector: Selection, vararg fields: Pair<String, Any?>): Int
}
