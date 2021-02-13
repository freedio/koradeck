package com.coradec.coradeck.type.ctrl

import kotlin.reflect.KClass

interface TypeConverter<TargetType: Any> {
    /** Checks if the type converter handles objects of the sp;ecified type. */
    fun handles(type: KClass<*>): Boolean
    /** Converts the specified value to a value of the target type. */
    fun convert(value: Any?): TargetType?
    /** Decodes the specified string representation to a value of the target type, or `null` if the representation is `null`. */
    fun decode(value: String?): TargetType?
}
