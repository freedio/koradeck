/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

interface LogEntry: Event {
    val level: LogLevel
    val worker: Thread
    val severe: Boolean get() = level.severity.severe

    /** Formats the log entry as a string with the specified Formatter format. */
    infix fun formattedWith(format: String): String
}
