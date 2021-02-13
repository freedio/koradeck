/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.module.impl

import com.coradec.coradeck.type.model.TypeConverters
import com.coradec.coradeck.type.module.CoraTypeAPI
import com.coradec.coradeck.type.trouble.MissingTypeArgumentsException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

@Suppress("UNCHECKED_CAST")
class CoraTypeImpl: CoraTypeAPI {
    override fun <T : Any> typeOf(klass: KClass<out T>, parameters: Map<String, KClass<*>>): KType {
        val typeParameters = klass.typeParameters.map { Pair(it.name, parameters[it.name]) }.toMap()
        typeParameters.filter { it.value == null }.let { missingArgs ->
            if (missingArgs.isNotEmpty()) throw MissingTypeArgumentsException(missingArgs.keys)
        }
        return klass.createType(/* TODO type arguments! */)
    }

    override fun <T : Any> castTo(value: Any?, type: KType): T? = type.classifier?.let { castTo(value, it as KClass<T>) }
    override fun <T : Any> castTo(value: Any?, type: KClass<T>): T? = TypeConverters[type].convert(value)
}
