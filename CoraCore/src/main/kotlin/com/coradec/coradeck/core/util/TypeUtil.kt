/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure

val KType.name: String
    get() = (classifier as KClass<*>).classname +
            (arguments.joinToString { it.type?.name ?: "*" }.let { if (it.isBlank()) "" else "<${it}>" })

operator fun KType.contains(other: KType) = isSubtypeOf(other)
operator fun KType.contains(instance: Any) = jvmErasure.isInstance(instance) && arguments.isEmpty() || when (jvmErasure) {
    in Collection::class -> try { checkCollectionMember(this, instance as Collection<*>) } catch (e: ClassCastException) { false }
    in Map::class -> try { checkMapMember(this, instance as Map<*, *>) } catch (e: ClassCastException) { false }
    else -> false // we assume that other types of generics don't match just like that
}

private fun checkCollectionMember(type: KType, instance: Collection<*>): Boolean =
    type.arguments.map { type.jvmErasure }.let { elementTypes ->
        if (elementTypes.size != 1) throw IllegalStateException("Expected one type argument, but got ${elementTypes.size}!")
        val elementType = elementTypes[0]
        instance.isEmpty() || elementType.isInstance(instance.iterator().next())
    }

private fun checkMapMember(type: KType, instance: Map<*, *>): Boolean =
    type.arguments.map { type.jvmErasure }.let { elementTypes ->
        if (elementTypes.size != 2) throw IllegalStateException("Expected two type arguments, but got ${elementTypes.size}!")
        val keyType = elementTypes[0]
        val valueType = elementTypes[1]
        instance.isEmpty() || with(instance.iterator().next()) { keyType.isInstance(key) && valueType.isInstance(value) }
    }
