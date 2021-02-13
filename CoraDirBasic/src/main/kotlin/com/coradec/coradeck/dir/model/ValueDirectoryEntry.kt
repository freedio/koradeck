package com.coradec.coradeck.dir.model

import com.coradec.coradeck.dir.model.impl.BasicDirectoryEntry

interface ValueDirectoryEntry<V>: DirectoryEntry {
    val value: V
}
