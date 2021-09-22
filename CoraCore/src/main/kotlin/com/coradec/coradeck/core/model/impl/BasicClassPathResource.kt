/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.ClassPathResource
import com.coradec.coradeck.core.trouble.ClassPathResourceNotFoundException
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset

class BasicClassPathResource(override val path: String, val encoding: Charset = Charsets.UTF_8) : ClassPathResource {
    private val resource: URL? by lazy { Thread.currentThread().contextClassLoader.getResource(path) }
    override val location: URL get() = resource ?: throw ClassPathResourceNotFoundException(path)
    override val exists: Boolean get() = resource != null
    override val stream: InputStream get() = location.openStream()
    override val content: String get() = stream.use { it.bufferedReader(encoding).readText() }
    override val lines: List<String> get() = stream.use { it.bufferedReader(encoding).readLines() }
    override val file: File
        get() = if (location.protocol == "file") File(location.path)
        else throw IllegalStateException("Class path resource «$location» is not a file!")

    override fun ifExists(function: ClassPathResource.() -> Unit): Boolean = (resource != null)
        .also { if (it) function.invoke(this) }
}
