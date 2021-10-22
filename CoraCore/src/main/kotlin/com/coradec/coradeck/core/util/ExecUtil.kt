/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.util

import com.coradec.coradeck.core.ctrl.ThreadMonitor
import com.coradec.coradeck.core.model.ClassPathResource
import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.model.StackTrace
import kotlin.reflect.KClass

private const val base = 3
val caller: StackFrame get() = getStackFrame(base)
val caller2: StackFrame get() = getStackFrame(base + 1)
val caller3: StackFrame get() = getStackFrame(base + 2)
val callerStack: StackTrace get() = StackTrace(Thread.currentThread().stackTrace.map { StackFrame(it) })
val here: StackFrame get() = caller2

fun getStackFrame(index: Int): StackFrame = StackFrame(Thread.currentThread().stackTrace
    .drop(index)
    .dropWhile { it.methodName.startsWith("access$") }
    .dropWhile { it.className.startsWith("jdk.") || it.className.startsWith("java.") }
    .dropWhile { "${it.className}.${it.methodName}" == "com.coradec.coradeck.core.util.ExecUtilKt.<clinit>" }
    .first()
)

fun callerStackFrame(after: String): StackFrame = StackFrame(Thread.currentThread().stackTrace
    .dropWhile { after !in it.className }
    .dropWhile { after in it.className }
    .first())

fun relax() {}
fun Thread.whenTerminated(action: (Thread) -> Unit) {
    ThreadMonitor.registerTerminationHook(this, action)
}

@Suppress("unused")
fun Any?.swallow() {}
fun execute(action: () -> Unit) {
    action.invoke()
}
fun addShutdownHook(name: String? = null, routine: () -> Unit) = with (Runtime.getRuntime()) {
    if (name != null) addShutdownHook(Thread(routine, name))
    else addShutdownHook(Thread(routine))
}

fun resource(klass: KClass<*>, ext: String) = ClassPathResource(klass, ext)
