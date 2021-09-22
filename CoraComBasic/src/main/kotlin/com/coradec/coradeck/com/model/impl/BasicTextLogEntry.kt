/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.LogLevel
import com.coradec.coradeck.com.model.TextLogEntry
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.text.model.Text

class BasicTextLogEntry(
    origin: Origin,
    level: LogLevel,
    val text: Text,
    private vararg val args: Any
) : BasicLogEntry(origin, level), TextLogEntry {
    override fun formattedWith(format: String): String =
            format.format(createdAt, worker.name, level.abbrev, String.format(text.get(), *args), origin.representation)
    override fun toString(): String = formattedWith(Syslog.FORMAT)
}
