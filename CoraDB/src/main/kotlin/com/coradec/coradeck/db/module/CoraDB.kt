/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.module

import com.coradec.coradeck.db.model.TableKind
import com.coradec.coradeck.dir.model.module.CoraModule
import kotlin.reflect.KClass

object CoraDB: CoraModule<CoraDBAPI>() {
    /** Gives access to a table (view, materialized view, ...) object with the specified model in the default DB. */
    fun <T: Any> Table(model: KClass<out T>): TableKind<T> = impl.defineTable(model)
}