package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.model.DirectoryEntryTemplate
import com.coradec.coradeck.dir.model.TypeDirectory

class BasicTypeDirectoryTemplate : BasicTypeDirectory(null, "?"), DirectoryEntryTemplate {
    override fun real(parent: Directory?, name: String): DirectoryEntry = BasicTypeDirectory(parent, name, typeMap)
}
