package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.LogEntry
import com.coradec.coradeck.com.model.LogLevel
import com.coradec.coradeck.core.model.Origin
import java.time.ZonedDateTime

abstract class BasicLogEntry(
        origin: Origin,
        override val level: LogLevel,
        override val worker: Thread = Thread.currentThread(),
        created: ZonedDateTime = ZonedDateTime.now()
) : BasicEvent(origin, false, created), LogEntry {

}
