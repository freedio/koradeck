/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.ctrl.impl

import com.coradec.coradeck.com.ctrl.Log
import com.coradec.coradeck.com.model.LogEntry
import com.coradec.coradeck.com.model.LogLevel.*
import com.coradec.coradeck.com.model.ProblemLogEntry
import com.coradec.coradeck.com.model.StringLogEntry
import com.coradec.coradeck.com.model.TextLogEntry
import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.util.caller2
import com.coradec.coradeck.text.model.Text

open class Logger {
    private val log = CoraCom.log

    @Deprecated("This doesn't work as expected!", replaceWith = ReplaceWith("error(template: Text)"))
    fun error(arg: Any) {
        TODO("you used the wrong 'error()' method, probably tried to specify a String")
    }

    protected fun log(entry: LogEntry) = log.log(entry)
    protected fun abort(problem: Throwable) = log(ProblemLogEntry(caller2, FATAL, problem))
    protected fun abort(template: Text, vararg args: Any) = log(TextLogEntry(caller2, FATAL, template, *args))
    protected fun error(problem: Throwable) = log(ProblemLogEntry(caller2, ERROR, problem))
    protected fun error(template: Text, vararg args: Any) = log(TextLogEntry(caller2, ERROR, template, *args))
    protected fun error(problem: Throwable, template: Text, vararg args: Any) = log(ProblemLogEntry(caller2, ERROR, problem, template, *args))
    protected fun warn(template: Text, vararg args: Any)  = log(TextLogEntry(caller2, WARNING, template, *args))
    protected fun warn(problem: Throwable, template: Text, vararg args: Any) = log(ProblemLogEntry(caller2, WARNING, problem, template, *args))
    protected fun alert(template: Text, vararg args: Any) = log(TextLogEntry(caller2, ALERT, template, *args))
    protected fun info(template: Text, vararg args: Any) = log(TextLogEntry(caller2, INFORMATION, template, *args))
    protected fun detail(template: Text, vararg args: Any) = log(TextLogEntry(caller2, DETAIL, template, *args))
    protected fun detail(text: String, vararg args: Any) = log(StringLogEntry(caller2, DETAIL, text, *args))
    protected fun debug(template: Text, vararg args: Any) = log(TextLogEntry(caller2, DEBUG, template, *args))
    protected fun debug(text: String, vararg args: Any) = log(StringLogEntry(caller2, DEBUG, text, *args))
    protected fun trace(template: Text, vararg args: Any) = log(TextLogEntry(caller2, TRACE, template, *args))
    protected fun trace(text: String, vararg args: Any) = log(StringLogEntry(caller2, TRACE, text, *args))
    protected fun subtrace(template: Text, vararg args: Any) = log(TextLogEntry(caller2, SUBTRACE, template, *args))
    protected fun subtrace(text: String, vararg args: Any) = log(StringLogEntry(caller2, SUBTRACE, text, *args))
    protected fun infra(template: Text, vararg args: Any) = log(TextLogEntry(caller2, INFRA, template, *args))
    protected fun infra(text: String, vararg args: Any) = log(StringLogEntry(caller2, INFRA, text, *args))

    // -----------------------------------------------------

    protected fun unimplemented() {
        fun format(frame: StackFrame): String = "${frame.className}.${frame.methodName}()"
        log(TextLogEntry(caller2, WARNING, Log.TEXT_NOT_IMPLEMENTED, format(caller2)))
    }
}
