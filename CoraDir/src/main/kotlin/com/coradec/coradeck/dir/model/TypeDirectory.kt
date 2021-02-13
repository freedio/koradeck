/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model

import com.coradec.coradeck.dir.module.CoraDir
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface TypeDirectory: DirectoryEntry {
    /** A collection of all the types in the directory. */
    val types: Collection<KType>
    /** Returns all implementations of the specified type. */
    operator fun <T: Any> get(type: KType): Collection<T>
    /** Returns all implementations of the specified type. */
    operator fun <T: Any> get(type: KClass<T>): Collection<T>
    /** Adds the specified type, if it has not yet been added. */
    operator fun <T: Any> plusAssign(obj: T)
    /** Adds the specified type, if it has not yet been added. */
    fun <T: Any> add(obj: T, klass: KClass<out T> = obj::class, parameters: Map<String, KClass<*>> = mapOf())
    /** Returns the type folder for the specified type, if exacly one exactly matching (super)type was defined, otherwise `null`. */
    fun <T: Any> getType(type: KType): TypeFolder<T>?
    /** Returns a list of type folders with concrete (instantiable) (sub)types of the specified type. */
    fun <T : Any> getTypes(type: KType): List<TypeFolder<T>>
    /** Adds a type folder for the specified type, if one does not yet exist, and returns its type folder. */
    fun <T: Any> addType(type: KType): TypeFolder<T>

    companion object {
        operator fun invoke(): TypeDirectory = CoraDir.createTypeDirectory()
    }
}
