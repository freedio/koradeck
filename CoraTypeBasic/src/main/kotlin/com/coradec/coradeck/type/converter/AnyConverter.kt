/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter

class AnyConverter: BasicTypeConverter<Any>(Any::class) {
    override fun decodeFrom(value: String): Any? = null
    override fun convertFrom(value: Any): Any = value
}