/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.module

import com.coradec.coradeck.dir.model.module.CoraModule
import com.coradec.coradeck.type.model.Password
import kotlin.reflect.KClass
import kotlin.reflect.KType

object CoraType: CoraModule<CoraTypeAPI>() {
    /** Creates a type from the specified class with the help of the specified type parameters, if necessary. */
    fun <T: Any> typeOf(klass: KClass<out T>, parameters: Map<String, KClass<*>>): KType = impl.typeOf(klass, parameters)
    /** Casts the specified value to the specified type, if possible, preserving `null` values. */
    fun <P> castTo(value: Any?, type: KType): P? = impl.castTo(value, type)
    /** Casts the specified value to the specified type, if possible, preserving `null` values. */
    fun <P: Any> castTo(value: Any?, type: KClass<P>): P? = impl.castTo(value, type)
    /** Creates a password with the specified cleartext representation. */
    fun password(cleartext: String): Password = impl.password(cleartext)

    fun Any?.toInt(): Int? = castTo(this, Int::class)
    fun Any?.toBoolean(): Boolean = castTo(this, Boolean::class) == true
}
