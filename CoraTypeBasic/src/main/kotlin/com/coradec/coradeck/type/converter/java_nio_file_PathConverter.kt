/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.core.util.USER_HOME
import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

class java_nio_file_PathConverter: BasicTypeConverter<Path>(Path::class) {
    override fun decodeFrom(value: String): Path? = Paths.get(value.replace(Regex("^~"), USER_HOME))
    override fun convertFrom(value: Any): Path? = when(value) {
        is File -> value.toPath()
        is URL -> Paths.get(value.path)
        else -> null
    }
}