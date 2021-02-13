package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.ClassPathResource
import java.net.URL

class BasicClassPathResource(val path: String) : ClassPathResource {
    private val resource: URL? by lazy { Thread.currentThread().contextClassLoader.getResource(path) }
    override val location: URL get() = resource ?: throw ClassPathResourceNotFound(path)

    override fun ifExists(function: ClassPathResource.() -> Unit): Boolean = (resource != null)
            .also { if (it) function.invoke(this) }
}
