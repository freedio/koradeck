/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.module

import kotlin.reflect.KClass
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

@Suppress("UNCHECKED_CAST")
open class CoraModule<M: CoraModuleAPI> {
    val type =
            this::class.supertypes.map { type ->
                type.arguments.single { it.type?.isSubtypeOf(CoraModuleAPIType) == true }
            }.single().type?.classifier as? KClass<out M> ?: throw IllegalArgumentException("$this is not a module type")
    val implementations: CoraModuleList<M> get() = CoraModules.implementations(this::class)
    val impl: M get() = CoraModules.implementations(this::class).best

    companion object {
        val CoraModuleAPIType = CoraModuleAPI::class.createType()
    }
}

