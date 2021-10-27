/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

val URL.lines: List<String> get() = openStream().bufferedReader().readLines()
fun String.toPath(): Path = Paths.get(this)
