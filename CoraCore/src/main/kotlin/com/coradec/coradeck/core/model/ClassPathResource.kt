package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicClassPathResource
import java.net.URL
import kotlin.reflect.KClass

interface ClassPathResource {
    val location: URL

    fun ifExists(function: ClassPathResource.() -> Unit): Boolean

    companion object {
        /** Creates a class path resource from the specified kotlin class, with the specified extension (must include the dot!) */
        operator fun invoke(klass: KClass<*>, ext: String): ClassPathResource =
                BasicClassPathResource(klass.java.name.replace('.', '/') + ext)
        /** Creates a class path resource from the specified path (must be relative to work properly!) */
        operator fun invoke(path: String): ClassPathResource = BasicClassPathResource(path)
    }
}
