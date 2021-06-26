package com.coradec.coradeck.type.model

import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.name
import com.coradec.coradeck.type.ctrl.TypeConverter
import com.coradec.coradeck.type.trouble.NonCompliantTypeConverterException
import com.coradec.coradeck.type.trouble.TypeConverterNotFoundException
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmName

@Suppress("UNCHECKED_CAST")
object TypeConverters {
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
            throw TypeConverterNotFoundException(type.classname, converterName)
        } catch (e: NoSuchMethodException) {
            throw NonCompliantTypeConverterException(converterClass, e)
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
            converterClass.getConstructor().newInstance()
        } catch (e: ClassNotFoundException) {
            throw TypeConverterNotFoundException(type.name, converterName)
        } catch (e: NoSuchMethodException) {
            throw NonCompliantTypeConverterException(converterClass, e)
        }
    }

}
