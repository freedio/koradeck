/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.module

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

object CoraModules {
    private val MODULE_API_TYPE = CoraModuleAPI::class.createType()
    private val implementations = ArrayList<CoraModuleAPI>()

    @Suppress("UNCHECKED_CAST")
    fun <M : CoraModuleAPI> implementations(klass: KClass<out CoraModule<M>>): CoraModuleList<M> =
            getModuleAPI(klass).let { type ->
                CoraModuleList(klass, implementations.filter { type == getModuleAPI2(it::class) } as List<M>)
            }

    private fun getModuleAPI(type: KClass<*>): KType =
            type.supertypes
                    .first { t -> t.arguments.any { it.type?.isSubtypeOf(MODULE_API_TYPE) == true } }
                    .arguments.single { it.type?.isSubtypeOf(MODULE_API_TYPE) == true }.type!!

    private fun getModuleAPI2(type: KClass<*>): KType =
            type.supertypes.first { t -> t.isSubtypeOf(MODULE_API_TYPE) }

    fun register(vararg impl: CoraModuleAPI) = synchronized(implementations) { implementations += impl }

    fun initialize() {
        implementations.clear()
    }
}
