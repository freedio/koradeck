package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.LogLevel
import com.coradec.coradeck.com.model.StringLogEntry
import com.coradec.coradeck.core.model.Origin

class BasicStringLogEntry(
        origin: Origin,
        level: LogLevel,
        private val text: String,
        private vararg val args: Any
) : BasicLogEntry(origin, level), StringLogEntry {
    override fun formattedWith(format: String): String =
            format.format(createdAt, worker.name, level.abbrev, text.format(*args), origin.representation)
    override fun toString(): String = formattedWith(Syslog.FORMAT)
}
