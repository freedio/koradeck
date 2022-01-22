/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.module.impl

import com.coradec.coradeck.type.ctrl.TypeConverter
import com.coradec.coradeck.type.model.Password
import com.coradec.coradeck.type.model.Secret
import com.coradec.coradeck.type.model.TypeConverters
import com.coradec.coradeck.type.model.impl.BasicPassword
import com.coradec.coradeck.type.model.impl.BasicSecret
import com.coradec.coradeck.type.module.CoraTypeAPI
import com.coradec.coradeck.type.trouble.MissingTypeArgumentsException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

@Suppress("UNCHECKED_CAST")
class CoraTypeImpl : CoraTypeAPI {
    override fun <T : Any> typeOf(klass: KClass<out T>, parameters: Map<String, KClass<*>>): KType {
        val typeParameters = klass.typeParameters.associate { Pair(it.name, parameters[it.name]) }
        typeParameters.filter { it.value == null }.let { missingArgs ->
            if (missingArgs.isNotEmpty()) throw MissingTypeArgumentsException(missingArgs.keys)
        }
        return klass.createType(/* TODO type arguments! */)
    }

    override fun <T : Any?> castTo(value: Any?, type: KType): T? = (TypeConverters[type] as TypeConverter<T>).convert(value)
    override fun <T : Any?> castTo(value: Any?, type: KClass<*>): T? = (TypeConverters[type] as TypeConverter<T>).convert(value)
    override fun password(cleartext: String): Password = BasicPassword(cleartext)
    override fun secret(cleartext: String): Secret = BasicSecret(cleartext)
}
