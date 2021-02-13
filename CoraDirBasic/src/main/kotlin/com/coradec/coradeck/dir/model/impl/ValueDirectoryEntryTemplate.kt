package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.model.ValueDirectoryEntry

class ValueDirectoryEntryTemplate<V>(override val value: V) :
        BasicDirectoryEntryTemplate(), ValueDirectoryEntry<V> {
    override fun real(parent: Directory?, name: String): DirectoryEntry = BasicValueDirectoryEntry(parent, name, value)
}
