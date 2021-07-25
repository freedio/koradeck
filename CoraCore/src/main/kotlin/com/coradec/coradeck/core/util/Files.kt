/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.TERMINATE
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

object Files {

    fun deleteTree(path: Path) {
        Files.walkFileTree(path, object : FileVisitor<Path> {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = CONTINUE
            override fun visitFile(file: Path, attrs: BasicFileAttributes) = CONTINUE.also { Files.delete(file) }
            override fun visitFileFailed(file: Path, exc: IOException) = TERMINATE.also { exc.printStackTrace() }
            override fun postVisitDirectory(dir: Path, exc: IOException?) = CONTINUE.also { Files.delete(dir) }
        })
    }
}

enum class FileType(val formatted: String) {
    REGULAR("f"),
    DIRECTORY("d"),
    SOCKET("s"),
    BLOCKDEVICE("b"),
    CHARDEVICE("c"),
    PIPE("p"),
    SYMLINK("l"),
    DOOR("D"),
    LOOP_LINK("L"),
    LOST_LINK("N");

    companion object {
        operator fun invoke(type: String): FileType = when (type) {
            "ff" -> REGULAR
            "dd" -> DIRECTORY
            "ss" -> SOCKET
            "bb" -> BLOCKDEVICE
            "cc" -> CHARDEVICE
            "pp" -> PIPE
            "DD" -> DOOR
            "lf" -> SYMLINK
            "ld" -> SYMLINK
            "ls" -> SYMLINK
            "lb" -> SYMLINK
            "lc" -> SYMLINK
            "lp" -> SYMLINK
            "lD" -> SYMLINK
            "lL" -> LOOP_LINK
            "lN" -> LOST_LINK
            else -> throw IllegalArgumentException("Unknown file type: ‹$type›!")
        }
    }
}
