/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.db.ctrl.impl.SqlSelection
import com.coradec.coradeck.db.model.RecordTable
import kotlin.reflect.KClass

class HsqlDbTable<Record: Any>(model: KClass<out Record>) : HsqlDbCollection<Record>(model), RecordTable<Record> {
    companion object {
        fun <Record: Any> create(model: KClass<out Record>): HsqlDbTable<Record> {
            TODO("Not yet implemented")
        }

        fun <Record: Any> open(model: KClass<out Record>): HsqlDbTable<Record> {
            TODO("Not yet implemented")
        }

        fun <Record: Any> creopen(model: KClass<out Record>): HsqlDbTable<Record> {
            TODO("Not yet implemented")
        }
    }

    override val selector = SqlSelection("")
}
