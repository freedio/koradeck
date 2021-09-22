/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.view

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.module.CoraDir

interface DirectoryView {
    /** The root path of the directory view. */
    val rootPath: String
    /** The current working path of the directory view. */
    var workingPath: String

    /** Retrieves the directory entry with the specified path relative to the local root. */
    operator fun get(path: String): DirectoryEntry?
    /** Adds the specified value as a value directory entry with the specified path relative to the local root. */
    operator fun set(path: String, value: Any)
    /** Adds the specified directory entry with the specified path relative to the local root. */
    operator fun set(path: String, entry: DirectoryEntry)
    /** Commits the changes to the view. */
    fun commit()
    /** Aborts the changes to the view. */
    fun abort()

    companion object {
        operator fun invoke(path: String, root: Directory? = null): DirectoryView = CoraDir.getDirectoryView(path, root)
    }
}
