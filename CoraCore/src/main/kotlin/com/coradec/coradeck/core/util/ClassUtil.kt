/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

val KClass<*>.classname: String
    get() = (qualifiedName ?: throw IllegalStateException("Class $this has no qualified name!"))
            .removePrefix("kotlin.")
            .removePrefix("java.lang.")
            .removePrefix("collections.")
val KClass<*>.shortClassname: String get() = (simpleName ?: throw IllegalStateException("Class $this has no simple name!"))
operator fun KClass<*>.contains(other: KClass<*>) = isSubclassOf(other)
operator fun KClass<*>.contains(instance: Any) = isInstance(instance)

fun genericToString(klass: KClass<*>, vararg fields: Pair<String, Any>): String {
    val collector = StringBuffer()
    collector.append('<').append(klass.classname)
    fields.forEach { (name, value) ->
        collector.append(' ').append(name).append('=').append(value.formatted)
    }
    return collector.toString()
}