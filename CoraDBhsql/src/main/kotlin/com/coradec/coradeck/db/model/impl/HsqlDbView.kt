/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.db.model.RecordTable
import com.coradec.coradeck.db.model.RecordView
import kotlin.reflect.KClass

class HsqlDbView<Record: Any>(model: KClass<out Record>) : RecordTable<Record> {
    override fun iterator(): Iterator<Record> {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    companion object {
        fun <Record: Any> create(model: KClass<out Record>): RecordView<Record> {
            TODO("Not yet implemented")
        }

        fun <Record: Any> open(model: KClass<out Record>): RecordView<Record> {
            TODO("Not yet implemented")
        }

        fun <Record: Any> creopen(model: KClass<out Record>): RecordView<Record> {
            TODO("Not yet implemented")
        }
    }
}
