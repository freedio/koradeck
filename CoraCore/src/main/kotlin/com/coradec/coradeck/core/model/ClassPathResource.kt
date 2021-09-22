/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicClassPathResource
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.reflect.KClass

interface ClassPathResource {
    val location: URL
    val exists: Boolean
    val stream: InputStream
    val lines: List<String>
    val path: String
    val content: String
    val file: File

    fun ifExists(function: ClassPathResource.() -> Unit): Boolean

    companion object {
        /**
         * Creates a class path resource from the specified kotlin class, with the specified extension (must include the dot!)
         * This method can also be used to refer to the class as a directory, containing the file specified in [ext].  In this case,
         * the file name ([ext]) must start with a slash, as in "/example.txt".
         */
        operator fun invoke(klass: KClass<*>, ext: String): ClassPathResource =
                BasicClassPathResource(klass.java.name.replace('.', '/') + ext)
        /** Creates a class path resource from the specified path (must be relative to work properly!) */
        operator fun invoke(path: String): ClassPathResource = BasicClassPathResource(path)
    }
}
