package com.coradec.coradeck.dir.model

interface DirectoryEntry {
    /** The complete absolute path to this directory. */
    val path: String
    /** The parent directory, unless this is the root. */
    val parent: Directory?
    /** The name of the entry. */
    val name: String

    /** Locks the directory entry for read with the specified key.  Fails if the entry already has a write lock. */
    fun readLock(key: Any)
    /** Unlocks the directory entry for read with the specified key. */
    fun readUnlock(key: Any)
    /** Locks the directory entry for write.  Fails if the entry already has a read or write lock. */
    fun writeLock(key: Any)
    /** Unlocks the directory entry for write with the specified key. */
    fun writeUnlock(key: Any)
}
