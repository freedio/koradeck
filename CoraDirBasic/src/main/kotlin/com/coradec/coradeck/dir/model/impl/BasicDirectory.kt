/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.model.impl

import com.coradec.coradeck.core.util.swallow
import com.coradec.coradeck.dir.model.*
import com.coradec.coradeck.dir.trouble.CreateLockFailedException
import com.coradec.coradeck.dir.trouble.DirectoryEntryAlreadyExistsException
import com.coradec.coradeck.dir.trouble.OperationWithoutLockException
import com.coradec.coradeck.session.model.Session
import java.util.concurrent.ConcurrentHashMap

open class BasicDirectory(
        parent: Directory?,
        name: String,
        override val namespace: DirectoryNamespace = parent?.namespace ?: RootNamespace
) : BasicDirectoryEntry(parent, name), Directory {
    protected val entries = ConcurrentHashMap<String, DirectoryEntry>()
    override val count: Int get() = entries.size
    override val countAll: Int get() = count + entries.values.filterIsInstance<Directory>().sumOf { it.countAll }
    override val root get() = parent?.root ?: this
    private val isRoot = parent == null
    private val myParent get() = parent!!
    override val path: String = if (isRoot) name else myParent.concatPaths(myParent.path, name)
    private val createLocks = mutableMapOf<Path, Any>()

    override fun get(path: Path): DirectoryEntry? = when {
        path.isEmpty -> this
        path.isAbsolute -> root[path.relative]
        path.isName -> entries[path]
        else -> withComposite(path) { dir, rest -> dir[rest] }
    }

    override fun add(path: Path, entry: DirectoryEntry): Unit = Session.current.let { session ->
        when {
            path.isEmpty -> throw DirectoryEntryAlreadyExistsException(this.path)
            path.isAbsolute -> root.add(path, entry)
            path.isName -> {
                if (!createLocked(session, path)) throw OperationWithoutLockException("add", "create", path.absolute)
                if (!entries.containsKey(path)) entries[path] = entry.real(path)
                else
                    throw DirectoryEntryAlreadyExistsException(path.absolute)
                createUnlock(session, path)
                println("Added $entry as ${path.absolute}")
            }
            else -> withComposite(path) { dir, rest -> dir.add(rest, entry) }.swallow()
        }
    }

    override fun createLock(key: Any, path: Path) = when {
        path.isEmpty -> throw CreateLockFailedException("Entry already exists!")
        path.isAbsolute -> throw CreateLockFailedException("Absolute paths not allowed!")
        path.isName ->
            if (path !in createLocks) createLocks[path] = key else throw CreateLockFailedException()
        else -> withComposite(path) { dir, rest -> dir.createLock(key, rest) }
                ?: createLocks.set(path, key).swallow()
    }

    override fun createUnlock(key: Any, path: Path) {
        if (createLocks[path] == key) createLocks.remove(path) else parent?.createUnlock(key, name with path)
    }

    override fun createLocked(key: Any, path: Path): Boolean =
            createLocks[path] == key || !isRoot && myParent.createLocked(key, name with path)

    override fun contains(element: DirectoryEntry): Boolean =
            entries.values.contains(element) || element is Directory && element.parent?.let { contains(it) } == true

    private fun <R> withComposite(path: Path, function: (Directory, Path) -> R): R? = path.split?.let { (head, body) ->
        entries[head].let { entry ->
            if (entry is Directory) function.invoke(entry, body) else null
        }
    }

    private fun DirectoryEntry.real(name: String): DirectoryEntry = when (this) {
        is DirectoryTemplate -> real(this@BasicDirectory, name, namespace)
        is DirectoryEntryTemplate -> real(this@BasicDirectory, name)
        else -> this
    }

    override fun concatPaths(path1: Path, path2: Path): Path = namespace.concat(path1, path2)
    override fun pathIsAbsolute(path: Path): Boolean = namespace.isAbsolute(path)
    override fun makePathRelative(path: Path): Path = namespace.makeRelative(path)
    override fun pathIsSelfRelative(path: Path): Boolean = namespace.isSelfRelative(path)
    override fun removeSelfReference(path: Path): Path = namespace.removeSelfReference(path)
    override fun pathIsParentRelative(path: Path): Boolean = namespace.isParentRelative(path)
    override fun removeParentReference(path: Path): Path = namespace.removeParentReference(path)
    override fun pathOf(name: Path): Path = namespace.concat(path, name)
    override fun pathIsName(path: Path): Boolean = entries.values
            .filterIsInstance<Directory>()
            .map { dir -> !path.startsWith(dir.nameWithSeparator) }.let {
                if (it.isEmpty()) namespace.isName(path) else it.single()
            }
    override fun splitPath(path: Path): Pair<Head, Body>? = entries.values
            .filterIsInstance<Directory>()
            .mapNotNull { dir -> dir.namespace.splitIfPrefixedWith(dir.name, path) }
            .singleOrNull()

    private val Path.isEmpty: Boolean get() = isEmpty()
    private val Path.isAbsolute: Boolean get() = root.pathIsAbsolute(this)
    private val Path.isName: Boolean get() = pathIsName(this)
    private val Path.relative: Path get() = root.makePathRelative(this)
    private val Path.split: Pair<Head, Body>? get() = splitPath(this)
    private val Path.absolute: Path get() = path with this
    private infix fun Path.with(other: Path) = concatPaths(this, other)
}
