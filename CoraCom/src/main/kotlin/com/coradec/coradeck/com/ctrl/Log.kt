/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.ctrl

import com.coradec.coradeck.com.model.LogEntry
import com.coradec.coradeck.com.model.LogLevel.*
import com.coradec.coradeck.com.model.ProblemLogEntry
import com.coradec.coradeck.com.model.StringLogEntry
import com.coradec.coradeck.com.model.TextLogEntry
import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.util.caller2
import com.coradec.coradeck.core.util.caller3
import com.coradec.coradeck.text.model.LocalText
import com.coradec.coradeck.text.model.Text

@Suppress("unused")
interface Log {
    @Deprecated("This doesn't work as expected!", replaceWith = ReplaceWith("error(template: Text)"))
    fun error(arg: Any) {
        TODO("you used the wrong 'error()' method, probably tried to specify a String")
    }

    fun log(entry: LogEntry)
    fun abort(problem: Throwable) = log(ProblemLogEntry(caller3, FATAL, problem))
    fun abort(template: Text, vararg args: Any) = log(TextLogEntry(caller3, FATAL, template, *args))
    fun abort(problem: Throwable?, template: Text, vararg args: Any) = log(
        if (problem == null) TextLogEntry(caller2, FATAL, template, *args)
        else ProblemLogEntry(caller2, FATAL, problem, template, *args)
    )
    fun error(problem: Throwable) = log(ProblemLogEntry(caller3, ERROR, problem))
    fun error(template: Text, vararg args: Any) = log(TextLogEntry(caller3, ERROR, template, *args))
    fun error(problem: Throwable?, template: Text, vararg args: Any) = log(
        if (problem == null) TextLogEntry(caller2, ERROR, template, *args)
        else ProblemLogEntry(caller2, ERROR, problem, template, *args)
    )
    fun warn(template: Text, vararg args: Any)  = log(TextLogEntry(caller3, WARNING, template, *args))
    fun warn(problem: Throwable?, template: Text, vararg args: Any) = log(
        if (problem == null) TextLogEntry(caller2, WARNING, template, *args)
        else ProblemLogEntry(caller2, WARNING, problem, template, *args)
    )
    fun alert(template: Text, vararg args: Any) = log(TextLogEntry(caller3, ALERT, template, *args))
    fun info(template: Text, vararg args: Any) = log(TextLogEntry(caller3, INFORMATION, template, *args))
    fun detail(template: Text, vararg args: Any) = log(TextLogEntry(caller3, DETAIL, template, *args))
    fun detail(text: String, vararg args: Any) = log(StringLogEntry(caller3, DETAIL, text, *args))
    fun debug(template: Text, vararg args: Any) = log(TextLogEntry(caller3, DEBUG, template, *args))
    fun debug(text: String, vararg args: Any) = log(StringLogEntry(caller3, DEBUG, text, *args))
    fun trace(template: Text, vararg args: Any) = log(TextLogEntry(caller3, TRACE, template, *args))
    fun trace(text: String, vararg args: Any) = log(StringLogEntry(caller3, TRACE, text, *args))
    fun subtrace(template: Text, vararg args: Any) = log(TextLogEntry(caller3, SUBTRACE, template, *args))
    fun subtrace(text: String, vararg args: Any) = log(StringLogEntry(caller3, SUBTRACE, text, *args))
    fun infra(template: Text, vararg args: Any) = log(TextLogEntry(caller3, INFRA, template, *args))
    fun infra(text: String, vararg args: Any) = log(StringLogEntry(caller3, INFRA, text, *args))

    // -----------------------------------------------------

    fun unimplemented() {
        fun format(frame: StackFrame): String = "${frame.className}.${frame.methodName}()"
        log(TextLogEntry(caller2, WARNING, TEXT_NOT_IMPLEMENTED, format(caller2)))
    }

    companion object {
        val TEXT_NOT_IMPLEMENTED = LocalText("unimplemented")
    }

}
