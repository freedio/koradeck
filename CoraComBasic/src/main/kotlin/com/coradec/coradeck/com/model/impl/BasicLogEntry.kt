package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.LogEntry
import com.coradec.coradeck.com.model.LogLevel
import com.coradec.coradeck.core.model.Origin

abstract class BasicLogEntry(
        origin: Origin,
        override val level: LogLevel,
        override val worker: Thread = Thread.currentThread(),
) : BasicEvent(origin), LogEntry
