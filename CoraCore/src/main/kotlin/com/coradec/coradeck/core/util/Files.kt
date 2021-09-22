/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.TERMINATE
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink

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
    LOST_LINK("N"),
    UNKNOWN("U");

    companion object {
        val Path.type: FileType get() = when {
            isDirectory() -> DIRECTORY
            isRegularFile() -> REGULAR
            isSymbolicLink() -> SYMLINK
            else -> {
                if (System.getProperty("os.name") == "Linux") {
                    Runtime.getRuntime().exec("ls -ld ${toString()}").let { process ->
                        with (process) {
                            inputStream.bufferedReader().readText().let { line ->
                                when (line[0]) {
                                    '-' -> REGULAR
                                    'd' -> DIRECTORY
                                    'c' -> CHARDEVICE
                                    'b' -> BLOCKDEVICE
                                    's' -> SOCKET
                                    'p' -> PIPE
                                    'l' -> SYMLINK
                                    else -> UNKNOWN
                                }
                            }.also { waitFor() }
                        }
                    }
                } else UNKNOWN
            }
        }
    }
}

fun Path.relativeTo(base: String): Path = Paths.get(base).relativize(this)