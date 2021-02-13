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
operator fun KClass<*>.contains(other: KClass<*>) = isSubclassOf(other)
operator fun KClass<*>.contains(instance: Any) = isInstance(instance)
