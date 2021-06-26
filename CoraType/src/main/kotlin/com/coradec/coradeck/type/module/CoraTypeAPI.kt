package com.coradec.coradeck.type.module

import com.coradec.coradeck.dir.model.module.CoraModuleAPI
import com.coradec.coradeck.type.model.Password
import com.coradec.coradeck.type.model.Secret
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface CoraTypeAPI : CoraModuleAPI {
    /** Creates a type from the specified class with the help of the specified type parameters, if necessary. */
    fun <T : Any> typeOf(klass: KClass<out T>, parameters: Map<String, KClass<*>>): KType
    /** Casts the specified value to the specified type, if possible, preserving `null` values. */
    fun <T : Any> castTo(value: Any?, type: KType): T?
    /** Casts the specified value to the specified type, if possible, preserving `null` values. */
    fun <T : Any> castTo(value: Any?, type: KClass<T>): T?
    /** Creates a password with the specified cleartext representation. */
    fun password(cleartext: String): Password
    /** Creates a secret from the specified cleartext representation. */
    fun secret(cleartext: String): Secret
}
