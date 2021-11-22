/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.session.view.View
import java.sql.Connection
import java.sql.Statement
import kotlin.reflect.KClass

interface Database: View {
    val connection: Connection
    val statement: Statement
    val memberView: MemberView

    /** Retrieves the existing table with the specified record model. */
    fun <Record: Any> getTable(model: KClass<out Record>): RecordTable<Record>
    /** Opens the table with the specified record model, creating it if necessary. */
    fun <Record: Any> openTable(model: KClass<out Record>): RecordTable<Record>
    /** Resets the database to the initial state after creation by deleting all structures. */
    fun reset()
    /** Closes the database. */
    fun close()
    /** Marks the current transaction as failed. */
    fun failed()
}
