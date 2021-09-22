/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.module

import com.coradec.coradeck.com.ctrl.Log
import com.coradec.coradeck.com.model.LogLevel
import com.coradec.coradeck.com.model.ProblemLogEntry
import com.coradec.coradeck.com.model.StringLogEntry
import com.coradec.coradeck.com.model.TextLogEntry
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.dir.model.module.CoraModule
import com.coradec.coradeck.text.model.Text

object CoraCom : CoraModule<CoraComAPI>() {
    /** The standard log. */
    val log: Log get() = impl.log

    /**
     * Creates a string log entry with the specified text (optionally fitted with the specified arguments) at the specified
     * log level from the specified origin.
     */
    fun createStringLogEntry(origin: Origin, level: LogLevel, text: String, vararg args: Any): StringLogEntry =
            impl.createStringLogEntry(origin, level, text, *args)

    /**
     * Creates a text log entry with the specified text (optionally fitted with the specified arguments) at the specified
     * log level from the specified origin.
     */
    fun createTextLogEntry(origin: Origin, level: LogLevel, text: Text, vararg args: Any): TextLogEntry =
            impl.createTextLogEntry(origin, level, text, *args)
    /**
     * Creates a problem log entry about the specified problem with the specified optional text (optionally fitted with
     * the specified arguments) at the specified log level from the specified origin.
     */
    fun createProblemLogEntry(
            origin: Origin, level: LogLevel, problem: Throwable, text: Text?, vararg args: Any): ProblemLogEntry =
            impl.createProblemLogEntry(origin, level, problem, text, *args)
}
