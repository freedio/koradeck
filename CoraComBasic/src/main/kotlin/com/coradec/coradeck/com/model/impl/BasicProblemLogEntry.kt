package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.LogLevel
import com.coradec.coradeck.com.model.ProblemLogEntry
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.shortClassname
import com.coradec.coradeck.text.model.Text
import java.io.PrintWriter
import java.io.StringWriter

class BasicProblemLogEntry(
    origin: Origin,
    level: LogLevel,
    override val problem: Throwable,
    val text: Text? = null,
    private vararg val args: Any
) : BasicLogEntry(origin, level), ProblemLogEntry {
    override fun formattedWith(format: String): String =
        "$format%n%s".format(
            createdAt, worker.name, level.abbrev,
            if (text != null) String.format(text.get(), *args)
            else "${problem::class.shortClassname}: ${problem.localizedMessage}",
            origin.representation, repr()
        )

    override fun toString(): String = formattedWith(Syslog.FORMAT)

    private fun repr(): String {
        val buffer = StringWriter()
        PrintWriter(buffer).use {
            problem.printStackTrace(it)
        }
        return buffer.toString()
    }
}
