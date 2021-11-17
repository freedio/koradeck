/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.bus.model.BusHub
import java.sql.Connection
import java.sql.Statement
import kotlin.reflect.KClass

interface Database: BusHub {
    val connection: Connection
    val statement: Statement

    fun <Record: Any> getTable(model: KClass<out Record>): RecordTable<Record>
    fun <Record: Any> openTable(model: KClass<out Record>): RecordTable<Record>
    fun close()
}
