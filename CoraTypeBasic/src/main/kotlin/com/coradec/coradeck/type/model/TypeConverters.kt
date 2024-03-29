/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.model

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.name
import com.coradec.coradeck.type.ctrl.TypeConverter
import com.coradec.coradeck.type.trouble.NonCompliantTypeConverterException
import com.coradec.coradeck.type.trouble.TypeConverterNotFoundException
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("UNCHECKED_CAST")
object TypeConverters: Logger() {
    private val registeredConverters = listOf<TypeConverter<*>>()
    operator fun <T : Any> get(type: KClass<T>): TypeConverter<T> =
            registeredConverters.firstOrNull { it.handles(type) } as? TypeConverter<T> ?: registerConverterFor(type)
    operator fun get(type: KType): TypeConverter<*> =
            registeredConverters.firstOrNull { it.handles(type) } ?: registerConverterFor(type)

    private fun <T : Any> registerConverterFor(type: KClass<T>): TypeConverter<T> {
        val typeName = type.classname.replace('.', '_')
        val converterName = "com.coradec.coradeck.type.converter.${typeName}Converter"
        var converterClass: Class<TypeConverter<T>>? = null
        return try {
            converterClass = Class.forName(converterName) as Class<TypeConverter<T>>
            if (!TypeConverter::class.java.isAssignableFrom(converterClass))
                throw ClassCastException("${converterClass.name} is not a type converter!")
            converterClass.getConstructor().newInstance()
        } catch (e: ClassNotFoundException) {
            throw TypeConverterNotFoundException(type.classname, converterName).apply { error(this) }
        } catch (e: NoSuchMethodException) {
            throw NonCompliantTypeConverterException(converterClass, e).apply { error(this) }
        }
    }

    private fun registerConverterFor(type: KType): TypeConverter<*> {
        val typeName = type.name
                .let { if (it.startsWith("Class<")) "Class" else it }
                .replace("<", "_of_")
                .replace('>', '_')
                .replace(", ", "_and_")
                .replace(",", "_and_")
                .replace("*", "any")
                .replace('.', '_')
        val converterName = "com.coradec.coradeck.type.converter.${typeName}Converter"
        var converterClass: Class<TypeConverter<*>>? = null
        return try {
            converterClass = Class.forName(converterName) as Class<TypeConverter<*>>
            if (!TypeConverter::class.java.isAssignableFrom(converterClass))
                throw ClassCastException("${converterClass.name} is not a type converter!")
            try {
                converterClass.getConstructor(KType::class.java).newInstance(type)
            } catch (e: NoSuchMethodException) {
                converterClass.getConstructor().newInstance()
            }
        } catch (e: ClassNotFoundException) {
            throw TypeConverterNotFoundException(type.name, converterName).apply { error(this) }
        } catch (e: NoSuchMethodException) {
            throw NonCompliantTypeConverterException(converterClass, e).apply { error(this) }
        }
    }

}
