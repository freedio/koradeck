/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model

import com.coradec.coradeck.dir.module.CoraDir

typealias Path = String
typealias Head = String
typealias Body = Path

interface Directory : DirectoryEntry {
    /** The namespace. */
    val namespace: DirectoryNamespace
    /** Number of entries in this directory. */
    val count: Int
    /** Number of entries in the structure under this directory. */
    val countAll: Int
    /** The root directory (dynamic). */
    val root: Directory
    /** The name with its trailing path separator. */
    val nameWithSeparator: String get() = namespace.nameWithSeparator(name)

    /** Returns the directory entry with the specified path (absolute or relative to here). */
    operator fun get(path: Path): DirectoryEntry?
    /** Adds the specified entry at the specified path (absolute or relative to here). */
    fun add(path: Path, entry: DirectoryEntry)

    /** Checks if this directory contains the specified element. */
    operator fun contains(element: DirectoryEntry): Boolean

    // Locking

    /** Locks the specified path for create with the specified token. */
    fun createLock(key: Any, path: Path)
    /** Removes the create lock from the specified path, if the token is the same as the specified token. */
    fun createUnlock(key: Any, path: Path)
    /** Checks if the specified path is create locked. */
    fun createLocked(key: Any, path: Path): Boolean

    // Path handling and tests

    /** Concatenates path1 and path2 with the context-specific path separator. */
    fun concatPaths(path1: Path, path2: Path): Path

    /** Checks if the specified path is to be considered absolute. */
    fun pathIsAbsolute(path: Path): Boolean

    /** Makes this (possibly absolute) path relative to this directory. */
    fun makePathRelative(path: Path): Path

    /** Checks whether the specified path is self-relative. */
    fun pathIsSelfRelative(path: Path): Boolean

    /** Returns a path without the leading self reference. */
    fun removeSelfReference(path: Path): Path

    /** Checks whether the specified path is parent-relative. */
    fun pathIsParentRelative(path: Path): Boolean

    /** Returns a path without the leading parent reference. */
    fun removeParentReference(path: Path): Path

    /** Returns the path of the specified name as an element of this directory. */
    fun pathOf(name: Path): Path

    /** Checks if the specified path is a single name (not a hierarchical structure). */
    fun pathIsName(path: Path): Boolean

    /** Split the specified path into its first element (head) and the rest (body). */
    fun splitPath(path: Path): Pair<Head, Body>?

    companion object {
        operator fun invoke(namespace: DirectoryNamespace? = null): Directory = CoraDir.createDirectory(namespace)
    }
}
