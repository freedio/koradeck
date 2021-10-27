/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.bus.model.impl.BasicBusNode
import com.coradec.coradeck.db.ctrl.Selection
import com.coradec.coradeck.db.ctrl.impl.HsqlEngine
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordCollection
import com.coradec.coradeck.db.trouble.DatabaseNotFoundException
import java.sql.Statement
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

abstract class HsqlDbCollection<Record: Any>(protected val model: KClass<out Record>): BasicBusNode(), RecordCollection<Record> {
    private val db: Database get() = context(5, TimeUnit.SECONDS)[Database::class] ?: throw DatabaseNotFoundException()
    private val statement: Statement get() = db.connection.createStatement()
    private val engine: HsqlEngine<Record> get() = HsqlEngine(model)
    protected abstract val selector: Selection

    override fun iterator(): Iterator<Record> = engine.query(selector).iterator()

    override fun close() {
        db.close()
    }


}
