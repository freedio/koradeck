package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.ValueDirectoryEntry

class BasicValueDirectoryEntry<V>(parent: Directory?, name: String, override val value: V) :
        BasicDirectoryEntry(parent, name), ValueDirectoryEntry<V> {

}
