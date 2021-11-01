/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Log
import com.coradec.coradeck.com.model.LogEntry
import com.coradec.coradeck.com.model.LogLevel

object Syslog : Log {
    const val FORMAT = "%tF %<tT.%<tL %-8.8s %-5.5s %s%n\t%s"
    private val channel1 = System.out
    private val channel2 = System.err
    private val threshold get() = (System.getProperty("log.level") ?: "DEBUG").let {
        if (it == "INFO") "INFORMATION" else it
    }
    private val THRESHOLD: LogLevel by lazy {
        try {
            LogLevel.valueOf(threshold)
        } catch (e: IllegalArgumentException) {
            LogLevel.DEBUG
        }
    }
    val level: LogLevel get() = THRESHOLD
    override fun log(entry: LogEntry) {
        if (entry.level atLeast THRESHOLD)
        with (if (entry.severe) channel2 else channel1) {
            println(entry formattedWith FORMAT)
            flush()
        }
    }

}
