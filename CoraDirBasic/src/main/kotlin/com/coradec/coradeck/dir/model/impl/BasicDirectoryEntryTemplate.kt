package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.model.DirectoryEntryTemplate
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.session.model.Session

open class BasicDirectoryEntryTemplate() : BasicDirectoryEntry(null, "?"), DirectoryEntryTemplate {
    override fun real(parent: Directory?, name: String): DirectoryEntry = BasicDirectoryEntry(parent, name)
}
