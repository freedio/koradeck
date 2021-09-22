/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.module

import com.coradec.coradeck.db.model.TableKind
import com.coradec.coradeck.dir.model.module.CoraModuleAPI
import kotlin.reflect.KClass

interface CoraDBAPI: CoraModuleAPI {
    /** Gives access to a table (view, materialized view, ...) object with the specified model in the default DB. */
    fun <T: Any> defineTable(model: KClass<out T>): TableKind<T>
}
