/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.tools

import com.coradec.coradeck.core.util.formatted
import com.coradec.coradeck.core.util.unescaped
import java.nio.file.FileSystems

fun main() {
    System.getProperties()
        .toList()
        .sortedBy { it.first as String }
        .forEach { (name, value) -> println("$name: ${value.formatted.unescaped}") }
    println()
    val filesys = FileSystems.getDefault()
    println("FileSystem: $filesys")
    println("Root Directories: ${filesys.rootDirectories.joinToString { it.formatted }}")
}
