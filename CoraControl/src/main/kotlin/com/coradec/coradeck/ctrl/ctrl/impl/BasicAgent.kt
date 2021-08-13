package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.impl.ActionCommand
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.util.caller
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl.EMS
import com.coradec.coradeck.ctrl.trouble.CommandNotApprovedException
import com.coradec.coradeck.ctrl.trouble.NoRouteForMessageException
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "LeakingThis")
open class BasicAgent(queueSize: Int = 1024) : Logger(), Agent {
    private val index = NEXT.getAndIncrement()
    private val routes = ConcurrentHashMap<Class<*>, (Any) -> Unit>()
    private val approvedCommands = CopyOnWriteArraySet(INTERNAL_COMMANDS)
    private val queue = LinkedBlockingDeque<Information>(queueSize)
    override val queueSize: Int get() = queue.size
    private val ready = AtomicBoolean(true)
    override val representation: String get() = "BasicAgent#$index"

    init {
        AGENTS[index] = this
    }

    override fun <I : Information> inject(message: I): I = message.also {
        if (message.urgent) queue.addFirst(message) else queue.addLast(message)
        if (message is Message) message.enqueue(this) else message.enqueue()
        EMS.execute(this)
    }

    override fun <I : Information> forward(message: I): I = message.also {
        val copy = message.copy
        if (copy.urgent) queue.addFirst(copy) else queue.addLast(copy)
        if (copy is Message) copy.enqueue(this) else copy.enqueue()
        EMS.execute(this)
    }

    fun inject(action: () -> Unit): ActionCommand = ActionCommand(caller, action).also {
        queue.addLast(it)
        it.enqueue(this)
        EMS.execute(this)
    }

    override fun trigger() = if (ready.get()) onMessage(queue.take().also { it.dispatch() }) else EMS.execute(this)
    override fun synchronize() {
        inject(Synchronization(caller)).standBy()
    }

    override fun approve(vararg cmd: KClass<out Command>) {
        approvedCommands.addAll(cmd.toList())
    }

    override fun disapprove(vararg cmd: KClass<out Command>) {
        approvedCommands.removeAll(cmd.toList())
    }

    protected open fun onMessage(message: Information) {
        ready.set(false)
        try {
            when (message) {
                is Command ->
                    if (approvedCommands.any { it.isInstance(message) })
                        try {
                            message.execute()
                        } catch (e: Throwable) {
                            error(e, TEXT_COMMAND_FAILED, message::class.classname, e.toString())
                            message.fail(e)
                        }
                    else {
                        message.fail(CommandNotApprovedException(message))
                        error(TEXT_MESSAGE_NOT_APPROVED, message)
                    }
                is Synchronization -> {
                    debug("Synchronization point «%s» reached", message)
                    message.succeed()
                }
                in routes.keys -> with(routes.filterKeys { it.isInstance(message) }.iterator().next().value) {
                    try {
                        invoke(message)
                    } catch (e: Throwable) {
                        error(e, TEXT_MESSAGE_FAILED, message::class.classname, e.toString())
                        if (message is Request) message.fail(e)
                    }
                }
                else -> {
                    if (message is Request) message.fail(NoRouteForMessageException(message))
                    error(TEXT_MESSAGE_NOT_UNDERSTOOD, message)
                }
            }
        } finally {
            ready.set(true)
        }
    }

    protected fun <T : Information> addRoute(type: Class<out T>, processor: (T) -> Unit) {
        inject(AddRouteCommand(caller, type, processor))
    }

    protected fun <T : Information> addRoute(type: KClass<out T>, processor: (T) -> Unit) {
        inject(AddRouteCommand(caller, type.java, processor))
    }

    protected fun removeRoute(type: Class<out Information>) {
        inject(RemoveRouteCommand(caller, type))
    }

    protected fun removeRoute(type: KClass<out Information>) {
        inject(RemoveRouteCommand(caller, type.java))
    }

    private inner class AddRouteCommand<T>(
        origin: Origin,
        private val type: Class<out Any>,
        private val processor: (T) -> Unit
    ) : BasicCommand(origin, urgent = true) {
        override val copy: AddRouteCommand<T> get() = AddRouteCommand<T>(origin, type, processor)

        override fun execute() {
            routes[type] = processor as (Any) -> Unit
        }

    }

    private inner class RemoveRouteCommand(
        origin: Origin,
        private val type: Class<out Any>
    ) : BasicCommand(origin, urgent = true) {
        override val copy: RemoveRouteCommand get() = RemoveRouteCommand(origin, type)

        override fun execute() {
            routes.remove(type)
        }

    }

    private inner class Synchronization(origin: Origin) : BasicRequest(origin)

    companion object {
        private val AGENTS = ConcurrentHashMap<Int, Agent>()
        private val NEXT = AtomicInteger(0)
        private val TEXT_MESSAGE_NOT_UNDERSTOOD = LocalText("MessageNotUnderstood1")
        private val TEXT_MESSAGE_NOT_APPROVED = LocalText("MessageNotApproved1")
        private val TEXT_COMMAND_FAILED = LocalText("CommandFailed2")
        private val TEXT_MESSAGE_FAILED = LocalText("MessageFailed2")
        private val INTERNAL_COMMANDS = listOf(
            AddRouteCommand::class,
            RemoveRouteCommand::class,
            MultiRequest::class,
            ActionCommand::class
        )
    }
}
