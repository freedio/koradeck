/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.model.DirectoryEntryTemplate
import kotlin.reflect.KType

class BasicTypeFolderTemplate<T: Any>(type: KType) : BasicTypeFolder<T>(null, "?", type), DirectoryEntryTemplate {
    override fun real(parent: Directory?, name: String): DirectoryEntry = BasicTypeFolder<T>(parent, name, type)
}
