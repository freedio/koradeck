/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.module.model

import com.coradec.coradeck.module.trouble.ModuleWithoutPrimaryConstructorException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

object CoraModules {
    private val MODULE_API_TYPE = CoraModuleAPI::class.createType()
    private val modules = HashSet<KClass<out CoraModuleAPI>>()
    private var impls = HashSet<CoraModuleAPI>()
    private val implementations: Set<CoraModuleAPI>
        get() = synchronized(impls) {
            impls.ifEmpty {
                modules
                    .map { it.primaryConstructor?.call() ?: throw ModuleWithoutPrimaryConstructorException(it).apply { printStackTrace() } }
                    .toSet()
                    .apply { impls += this }
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun <M : CoraModuleAPI> implementations(klass: KClass<out CoraModule<M>>): CoraModuleList<M> = getModuleAPI(klass).let { type ->
        CoraModuleList(klass, implementations.filter { type == getModuleAPI2(it::class) } as List<M>)
    }

    private fun getModuleAPI(type: KClass<*>): KType = type.supertypes
        .first { t -> t.arguments.any { it.type?.isSubtypeOf(MODULE_API_TYPE) == true } }
        .arguments.single { it.type?.isSubtypeOf(MODULE_API_TYPE) == true }.type!!

    private fun getModuleAPI2(type: KClass<*>): KType =
        type.supertypes.first { t -> t.isSubtypeOf(MODULE_API_TYPE) }

    fun register(vararg impl: KClass<out CoraModuleAPI>) = synchronized(impls) {
        if (modules.addAll(impl.toSet())) impls.clear()
    }

    fun initialize() = synchronized(impls) {
        modules.clear()
        impls.clear()
    }
}
