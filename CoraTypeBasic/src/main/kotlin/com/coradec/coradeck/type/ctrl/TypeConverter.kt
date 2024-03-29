/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.ctrl

import kotlin.reflect.KClass
import kotlin.reflect.KType

interface TypeConverter<TargetType: Any?> {
    /** Checks if the type converter handles objects of the specified type. */
    fun handles(klass: KClass<*>): Boolean
    /** Checks if the type converter handles objects of the specified type. */
    fun handles(type: KType): Boolean
    /** Converts the specified value to a value of the target type, preserving `null` values. */
    fun convert(value: Any?): TargetType?
    /** Decodes the specified string value to a value of the target type, preserving `null` values. */
    fun decode(value: String?): TargetType?
}
