/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.trouble.ReadLockFailedException
import com.coradec.coradeck.dir.trouble.WriteLockFailedException

open class BasicDirectoryEntry(override val parent: Directory?, override val name: String) : DirectoryEntry {
    override val path: String get() = parent?.pathOf(name) ?: name
    private val readLocks = mutableListOf<Any>()
    private var writeLock: Any? = null

    override fun readLock(key: Any) {
        if (writeLock != null && writeLock != key) throw ReadLockFailedException()
        readLocks += key
    }

    override fun readUnlock(key: Any) {
        readLocks.removeIf { it == key }
    }

    override fun writeLock(key: Any) {
        if (writeLock != null && writeLock != key || readLocks.any { it != key }) throw WriteLockFailedException()
        writeLock = key
    }

    override fun writeUnlock(key: Any) {
        if (writeLock == key) writeLock = null
    }

    override fun clearLocks(key: Any) {
        writeUnlock(key)
        readUnlock(key)
    }
}
