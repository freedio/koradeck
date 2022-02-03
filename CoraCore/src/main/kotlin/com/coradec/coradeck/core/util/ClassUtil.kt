/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import com.coradec.coradeck.core.annot.NonRepresentable
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.impl.ClassOrigin
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KVisibility.PUBLIC
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmName

val KClass<*>.classname: String
    get() = (qualifiedName ?: jvmName)
        .removePrefix("kotlin.")
        .removePrefix("java.lang.")
        .removePrefix("java.util.")
        .removePrefix("collections.")
        .removePrefix("sequences.")
        .removePrefix("reflect.")
        .removeSuffix(".Companion")
val KClass<*>.shortClassname: String get() = simpleName ?: classname
val KClass<*>.simpleClassname: String get() = simpleName ?: classname
val Any.classname: String get() = this::class.classname
val Any.shortClassname: String get() = this::class.shortClassname
val Any.simpleClassname: String get() = this::class.shortClassname
val Any.identityHashCode: Int get() = System.identityHashCode(this)
val KClass<*>.asOrigin: Origin get() = ClassOrigin(this)
val Any.asOrigin: Origin get() = this::class.asOrigin
val Any.properties: Map<String, Any?>
    get() =
        this::class.memberProperties
            .filter { prop -> prop.visibility == PUBLIC && prop.findAnnotation<NonRepresentable>() == null }
            .associate { prop ->
                Pair(
                    prop.name, try {
                        prop.call(this@properties)
                    } catch (e: IllegalCallableAccessException) {
                        println("Property $classname.${prop.name} is not accessible! → ignored")
                        e.printStackTrace()
                    }
                )
            }

operator fun KClass<*>.contains(other: KClass<*>) = isSuperclassOf(other)
operator fun KClass<*>.contains(type: KType) = isSuperclassOf(type.classifier as KClass<*>)
operator fun KClass<*>.contains(instance: Any) = isInstance(instance)
operator fun Set<Class<*>>.contains(instance: Any) = any { it.isInstance(instance) }

fun genericToString(klass: KClass<*>, vararg fields: Pair<String, Any>): String {
    val collector = StringBuffer()
    collector.append('<').append(klass.classname)
    fields.forEach { (name, value) ->
        collector.append(' ').append(name).append('=').append(value.formatted)
    }
    return collector.toString()
}
