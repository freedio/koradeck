package com.coradec.coradeck.type.model

import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.type.ctrl.TypeConverter
import com.coradec.coradeck.type.trouble.NonCompliantTypeConverterException
import com.coradec.coradeck.type.trouble.TypeConverterNotFoundException
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
object TypeConverters {
    private val registeredConverters = listOf<TypeConverter<*>>()
    operator fun <T : Any> get(type: KClass<T>): TypeConverter<T> =
            registeredConverters.firstOrNull { it.handles(type) } as? TypeConverter<T> ?: registerConverterFor(type)

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
            throw TypeConverterNotFoundException(type, converterName)
        } catch (e: NoSuchMethodException) {
            throw NonCompliantTypeConverterException(converterClass, e)
        }
    }

}
