/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.TypeDirectory
import com.coradec.coradeck.dir.model.TypeFolder
import com.coradec.coradeck.type.module.CoraType
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSupertypeOf

@Suppress("UNCHECKED_CAST")
open class BasicTypeDirectory(
        parent: Directory?,
        name: String,
        presets: Map<KType, TypeFolder<*>> = mapOf()
) : BasicDirectoryEntry(parent, name), TypeDirectory {
    protected val typeMap: MutableMap<KType, TypeFolder<*>> = ConcurrentHashMap(presets)
    override val types: Collection<KType> get() = typeMap.keys

    override fun <T : Any> get(type: KType): Collection<T> {
        TODO("Not yet implemented")
    }

    override fun <T : Any> get(type: KClass<T>): Collection<T> =
            (typeMap.mapKeys { it.key.classifier } as Map<KClass<T>, TypeFolder<T>>)
                    .filter { it.key.isSubclassOf(type) }
                    .values.flatMap { it.instances }

    override fun <T: Any> plusAssign(obj: T) {
        add(obj)
    }

    override fun <T : Any> add(obj: T, klass: KClass<out T>, parameters: Map<String, KClass<*>>) {
        addType<T>(CoraType.typeOf(klass, parameters)).addInstance(obj)
    }

    override fun <T : Any> getType(type: KType): TypeFolder<T>? =
        typeMap.filterKeys { it.isSupertypeOf(type) && it.isSubtypeOf(type) }.ifEmpty {
            typeMap.filterKeys { it.isSubtypeOf(type) }
        }.values.filterIsInstance<TypeFolder<T>>().singleOrNull()

    override fun <T : Any> getTypes(type: KType): List<TypeFolder<T>> =
            typeMap.filterKeys { it.isSubtypeOf(type) }.values.filterIsInstance<TypeFolder<T>>()

    override fun <T : Any> addType(type: KType): TypeFolder<T> = getType(type) ?: TypeFolder<T>(type).apply {
        typeMap[type] = this
    }
}

