/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

val KType.name: String
    get() = (classifier as KClass<*>).classname +
            (arguments.joinToString { it.type?.name ?: "*" }.let { if (it.isBlank()) "" else "<${it}>" })
operator fun KType.contains(other: KType) = isSubtypeOf(other)
operator fun KType.contains(instance: Any) = this.jvmErasure.isInstance(instance)
