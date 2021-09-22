/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.view.impl

import com.coradec.coradeck.dir.model.Directory
import com.coradec.coradeck.dir.model.DirectoryEntry
import com.coradec.coradeck.dir.model.DirectoryTemplate
import com.coradec.coradeck.dir.model.Path
import com.coradec.coradeck.dir.model.impl.ValueDirectoryEntryTemplate
import com.coradec.coradeck.dir.trouble.DirectoryEntryAlreadyExistsException
import com.coradec.coradeck.dir.trouble.DirectoryNotFoundException
import com.coradec.coradeck.dir.trouble.PathUnresolvableException
import com.coradec.coradeck.dir.trouble.RootDirectoryHasNoParentException
import com.coradec.coradeck.dir.view.DirectoryView
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.session.view.impl.BasicView

/**
 * A view to transactionally handle the directory tree from the specified "root" path (absolute or relative to root) in the
 * context of the specified session.
 *
 * Directory Views are designed to be used by one thread only and are therefore not thread-safe.
 */
class BasicDirectoryView(override val rootPath: String, root: Directory) : BasicView(Session.current), DirectoryView {
    private val rootDir: Directory = when (val entry = root[rootPath]) {
        is Directory -> entry
        else -> throw IllegalArgumentException("\"${rootPath}\": Not a directory!")
    }
    private var workingDir = rootDir
    override var workingPath: String = ""
        set(value) {
            val dir = get(value) ?: throw DirectoryNotFoundException(value)
            if (dir !is Directory) throw DirectoryNotFoundException(value)
            field = value
            workingDir = dir
        }

    private val addedEntries = mutableMapOf<Path, DirectoryEntry>()
    private val removedEntries = mutableSetOf<Path>()
    private val changes = mutableListOf<DirChange>()
    private val readlocks = mutableSetOf<Path>()
    private val writelocks = mutableSetOf<Path>()

    override fun get(path: Path): DirectoryEntry? = realPath(path).let { realPath ->
        when (realPath) {
            in removedEntries -> null
            in addedEntries -> addedEntries[realPath]
            else -> try {
                rootDir[realPath]?.apply { readLock(session); readlocks += realPath }
            } catch (e: PathUnresolvableException) {
                null
            }
        }
    }

    override fun set(path: Path, value: Any) = set(path, ValueDirectoryEntryTemplate(value))

    override fun set(path: Path, entry: DirectoryEntry) {
        realPath(path).let { realPath ->
            if (realPath in removedEntries) removedEntries.remove(realPath)
            val dir = get(realPath)
            if (dir is Directory && isOutside(dir, rootDir)) throw DirectoryEntryAlreadyExistsException(realPath)
            rootDir[realPath]?.apply { writeLock(session) }
            val parentCheck = parentExists(rootDir, "", realPath)
            if (!parentCheck.exists)
                throw DirectoryNotFoundException(parentCheck.path)
            addedEntries[realPath] = entry
            rootDir.createLock(session, realPath)
            changes += EntryAdded(realPath, entry)
            writelocks += realPath
        }
    }

    private fun parentExists(dir: Directory, dirPath: Path, path: Path): ParentCheck = when {
        path.isEmpty() -> throw IllegalArgumentException("Path is empty!")
        dir.pathIsAbsolute(path) -> parentExists(rootDir, "", dir.makePathRelative(path))
        addedEntries
                .any { (name, entry) ->
                    entry is DirectoryTemplate && dir.pathIsName(path.removePrefix(entry.nameWithSeparator(name))) } ->
            ParentCheck(true, dirPath)
        dir.pathIsName(path) -> ParentCheck(true, dirPath)
        else -> dir.splitPath(path)?.let { (name, rest) ->
            val subdirName: Path = if (dirPath.isEmpty()) name else dir.concatPaths(dirPath, name)
            val addedEntry = addedEntries[subdirName]
            if (addedEntry is Directory && addedEntry.pathIsName(rest)) ParentCheck(true, subdirName)
            else {
                val subdir = dir[name]
                if (subdir == null || subdir !is Directory) ParentCheck(false, subdirName)
                else parentExists(subdir, subdirName, rest)
            }
        } ?: ParentCheck(false, path)
    }

    private fun isOutside(entry: Directory, scope: Directory): Boolean = entry == scope || entry.contains(scope)

    override fun commit() {
        changes.forEach { change -> change.apply(rootDir) }
        changes.clear()
        writelocks.forEach { path -> rootDir[path]?.writeUnlock(session) }
        writelocks.clear()
        readlocks.forEach { path -> rootDir[path]?.readUnlock(session) }
        readlocks.clear()
    }

    override fun abort() {
        changes.clear()
        writelocks.clear()
        readlocks.clear()
        addedEntries.clear()
        removedEntries.clear()
    }

    private fun realPath(path: String): String {
        var result = path
        var stayInLoop = true
        var currentDir = workingDir
        var currentPath = workingPath
        if (rootDir.pathIsAbsolute(result)) result = rootDir.makePathRelative(result)
        while (stayInLoop) when {
            currentDir.pathIsSelfRelative(result) ->
                result = currentDir.concatPaths(currentPath, currentDir.removeSelfReference(path))
            currentDir.parent?.pathIsParentRelative(result) == true -> {
                if (currentPath.isEmpty()) throw RootDirectoryHasNoParentException()
                currentDir = currentDir.parent ?: throw RootDirectoryHasNoParentException()
                currentPath = currentDir.path
                result = rootDir.makePathRelative(currentDir.concatPaths(currentPath, currentDir.removeParentReference(result)))
            }
            else -> stayInLoop = false
        }
        return result
    }

    data class ParentCheck(val exists: Boolean, val path: Path)

    interface DirChange {
        fun apply(target: Directory)
    }

    inner class EntryAdded(private val path: Path, private val entry: DirectoryEntry) : DirChange {
        override fun apply(target: Directory) {
            println("Executing $this")
            target.add(path, entry)
            addedEntries.remove(path)
        }

        override fun toString() = "AddEntry(path: \"$path\", entry: $entry)"
    }

}
