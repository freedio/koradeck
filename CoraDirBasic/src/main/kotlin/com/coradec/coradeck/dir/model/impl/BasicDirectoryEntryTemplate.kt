/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.model.DirectoryEntryTemplate

open class BasicDirectoryEntryTemplate() : BasicDirectoryEntry(null, "?"), DirectoryEntryTemplate {
    override fun real(parent: Directory?, name: String): DirectoryEntry = BasicDirectoryEntry(parent, name)
}
