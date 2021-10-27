/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import java.io.IOException
import java.nio.file.*
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.FileVisitResult.TERMINATE
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink

object Files {

    fun deleteTree(path: Path) {
        if (path.exists(NOFOLLOW_LINKS)) Files.walkFileTree(path, object : FileVisitor<Path> {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = CONTINUE
            override fun visitFile(file: Path, attrs: BasicFileAttributes) = CONTINUE.also { Files.delete(file) }
            override fun visitFileFailed(file: Path, exc: IOException) = TERMINATE.also { exc.printStackTrace() }
            override fun postVisitDirectory(dir: Path, exc: IOException?) = CONTINUE.also { Files.delete(dir) }
        })
    }
}

enum class FileType(val formatted: String, val label: String) {
    REGULAR("f", "Regular"),
    DIRECTORY("d", "Directory"),
    SOCKET("s", "Socket"),
    BLOCKDEVICE("b", "Block Device"),
    CHARDEVICE("c", "Character Device"),
    PIPE("p", "Pipe"),
    SYMLINK("l", "Symbolic Link"),
    DOOR("D", "Door"),
    LOOP_LINK("L", "Loop Link"),
    LOST_LINK("N", "Lost Link"),
    UNKNOWN("U", "Unknown");

    companion object {
        operator fun invoke(code: Char) = values().single { it.formatted[0] == code }
        operator fun invoke(name: String) = values().single { it.name == name || it.formatted == name || it.label == name }
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
