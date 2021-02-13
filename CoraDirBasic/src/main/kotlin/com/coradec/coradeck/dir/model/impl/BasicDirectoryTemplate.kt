package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryNamespace
import com.coradec.coradeck.dir.model.DirectoryTemplate
import com.coradec.coradeck.dir.model.Path

class BasicDirectoryTemplate(private val nameSpace: DirectoryNamespace? = null) :
        BasicDirectory(null, "?", nameSpace ?: RootNamespace), DirectoryTemplate {
    override val root: Directory get() = throw IllegalStateException("Cannot determine root directory of a template!")

    override fun real(parent: Directory?, name: String, namespace: DirectoryNamespace): Directory =
            BasicDirectory(parent, name, nameSpace ?: namespace)

    override fun nameWithSeparator(name: Path): String = namespace.nameWithSeparator(name)
}
