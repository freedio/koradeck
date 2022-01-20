/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import com.coradec.coradeck.core.trouble.BasicException

val NEWLINE: String get() = System.getProperty("line.separator")
const val LETTER_ESCAPES = "abfnrt0"
val USER_HOME: String get() = System.getProperty("user.home")
val CURRENT_DIR: String get() = System.getProperty("user.dir")
val TEMP_DIR: String get() = System.getProperty("java.io.tmpdir")
val CONFIG_DIR: String get() = when (val os = System.getProperty("os.name")) {
    "Mac OS X" -> "/etc/coradec"
    "Linux" -> "/etc/coradec"
    "Windows" -> "$USER_HOME\\AppData\\Roaming\\Coradec"
    else -> throw BasicException("Unknown Operating System: $os")
}
var APPLICATION: String = "" // if required, must be set when application starts
