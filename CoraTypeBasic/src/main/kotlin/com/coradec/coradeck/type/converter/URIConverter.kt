/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import java.io.File
import java.net.URI
import java.net.URL

class URIConverter(): BasicTypeConverter<URI>(URI::class) {
    override fun decodeFrom(value: String): URI = URI(value)
    override fun convertFrom(value: Any): URI? = when (value) {
        is URL -> value.toURI()
        is File -> value.toURI()
        else -> null
    }
}
