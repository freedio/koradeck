package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicClassPathResource
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Path
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
        /** Creates a class path resource from the specified kotlin class, with the specified extension (must include the dot!) */
        operator fun invoke(klass: KClass<*>, ext: String): ClassPathResource =
                BasicClassPathResource(klass.java.name.replace('.', '/') + '/' + ext)
        /** Creates a class path resource from the specified path (must be relative to work properly!) */
        operator fun invoke(path: String): ClassPathResource = BasicClassPathResource(path)
    }
}
