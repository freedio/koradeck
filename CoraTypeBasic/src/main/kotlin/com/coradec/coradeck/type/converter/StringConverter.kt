/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class StringConverter(): BasicTypeConverter<String>(String::class) {
    override fun decodeFrom(value: String): String = value
    override fun convertFrom(value: Any): String = value.toString()
}
