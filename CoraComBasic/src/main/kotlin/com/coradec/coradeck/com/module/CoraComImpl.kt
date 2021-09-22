/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.module

import com.coradec.coradeck.com.ctrl.Log
import com.coradec.coradeck.com.model.LogLevel
import com.coradec.coradeck.com.model.StringLogEntry
import com.coradec.coradeck.com.model.TextLogEntry
import com.coradec.coradeck.com.model.impl.BasicProblemLogEntry
import com.coradec.coradeck.com.model.impl.BasicStringLogEntry
import com.coradec.coradeck.com.model.impl.BasicTextLogEntry
import com.coradec.coradeck.com.model.impl.Syslog
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.text.model.Text

class CoraComImpl: CoraComAPI {
    override var log: Log = Syslog

    override fun createStringLogEntry(origin: Origin, level: LogLevel, text: String, vararg args: Any): StringLogEntry =
            BasicStringLogEntry(origin, level, text, *args)

    override fun createTextLogEntry(origin: Origin, level: LogLevel, text: Text, vararg args: Any): TextLogEntry =
            BasicTextLogEntry(origin, level, text, *args)

    override fun createProblemLogEntry(origin: Origin, level: LogLevel, problem: Throwable, text: Text?, vararg args: Any) =
            BasicProblemLogEntry(origin, level, problem, text, *args)
}
