package com.coradec.coradeck.core.ctrl

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ThreadMonitor {
    val terminationHooks = ConcurrentHashMap<Thread, MutableList<(Thread) -> Unit>>()
    val runningCount: Int get() = terminationHooks.size
    val monitor = object: Thread("ThreadMonitor") {
        override fun run() {
            while(true) {
                val i = terminationHooks.iterator()
                while (i.hasNext()) {
                    val (thread, actions) = i.next()
                    if (!thread.isAlive) {
                        actions.forEach { action -> action.invoke(thread) }
                        i.remove()
                    }
                }
                sleep(1000)
            }
        }
    }.apply { start() }

    fun registerTerminationHook(thread: Thread, action: (Thread) -> Unit) {
        terminationHooks.computeIfAbsent(thread) { Collections.synchronizedList(ArrayList()) } += action
    }

}
