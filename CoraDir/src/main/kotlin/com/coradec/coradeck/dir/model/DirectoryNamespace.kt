/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model

interface DirectoryNamespace {
    /** Returns a path this is a concatenation of path1 and path2. */
    fun concat(path1: Path, path2: Path): Path
    /** Checks if the specified path is to be considered absolute (in Unix: a path starting with "/"). */
    fun isAbsolute(path: Path): Boolean
    /** Removes the "absolute path" indicator from the path (if there is one), thus making it relative. */
    fun makeRelative(path: Path): Path
    /** Checks if the specified path starts with a self-relative marker (in Unix: "./"). */
    fun isSelfRelative(path: Path): Boolean
    /** Removes the self-relative marker from the path, if there is one. */
    fun removeSelfReference(path: Path): Path
    /** Checks if the specified path starts with a parent-relative marker (in Unix: "../". */
    fun isParentRelative(path: Path): Boolean
    /** Removes the parent-relative marker from the path, if there is one. */
    fun removeParentReference(path: Path): Path
    /** Checks if the specified path is a single name (not a hierarchical structure). */
    fun isName(path: Path): Boolean
    /** Splits the specified path into its head (first element) and body (the rest). */
    fun split(path: Path): Pair<Head, Body>
    /** Returns the specified directory name with its trailing path separator. */
    fun nameWithSeparator(name: String): Path
    /** Splits the specified path into its head and body if the head becomes the specified name. */
    fun splitIfPrefixedWith(name: String, path: Path): Pair<Head, Body>?
}
