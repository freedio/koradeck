package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.model.Command
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.caller
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.Semaphore

@Suppress("UNCHECKED_CAST")
open class BasicAgent : Agent {
    val routeLock = Semaphore(1)
    val approLock = Semaphore(1)
    private val routes = ConcurrentHashMap<Class<*>, (Any) -> Unit>()
    private val approvedCommands = CopyOnWriteArraySet(INTERNAL_COMMANDS)
    private val queue = LinkedBlockingDeque<Information>()

    override fun <I : Information> inject(info: I): I = info.also {
        if (info.urgent) queue.addFirst(info) else queue.addLast(info)
        CoraControl.EMS.execute(this)
    }

    override fun trigger() = onMessage(queue.take())

    override fun onMessage(message: Information) {
        TODO("Not yet implemented")
    }

    protected fun <T : Information> addRoute(type: Class<out T>, processor: (T) -> Unit) {
        inject(AddRouteCommand(caller, this, type, processor))
    }

    protected fun removeRoute(type: Class<out Information>) {
        inject(RemoveRouteCommand(caller, this, type))
    }

    inner class AddRouteCommand<T>(
            origin: Origin,
            recipient: Recipient,
            private val type: Class<out Any>,
            private val processor: (T) -> Unit
    ) : BasicCommand(origin, recipient) {
        override fun execute() {
            routes[type] = processor as (Any) -> Unit
        }

    }

    inner class RemoveRouteCommand(
            origin: Origin,
            recipient: Recipient,
            private val type: Class<out Any>
    ) : BasicCommand(origin, recipient) {
        override fun execute() {
            routes.remove(type)
        }

    }

    companion object {
        private val INTERNAL_COMMANDS = listOf<Class<out Command>>(
                AddRouteCommand::class.java,
                RemoveRouteCommand::class.java,
//                MultiRequest::class.java,
//                ExecuteCapturedCommand::class.java,
//                AwaitReleaseTrigger::class.java,
//                DummyRequest::class.java,
//                LambdaCommand::class.java
        )
    }
}
