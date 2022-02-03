/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model

interface DirectoryNode {
    val namespace: DirectoryNamespace

    /** Returns the directory entry associated with the specified path, if present. */
    operator fun get(path: String): DirectoryEntry?
    /** Adds or replaces the directory entry associated with the specified path by the specified value. */
    operator fun set(path: String, value: DirectoryEntry)
    /** Removes the directory entry associated with the specified path, if present. */
    operator fun minusAssign(path: String) = del(path)
    fun del(path: String)

    /** Applies the specified action to all members of this node recursively, depth first. */
    fun propagate(action: (DirectoryEntry) -> Any)
    /** Reports if the directory node is empty (has no members). */
    fun isEmpty(): Boolean
}
