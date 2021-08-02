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
import com.coradec.coradeck.core.util.here
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl.EMS
import com.coradec.coradeck.ctrl.trouble.CommandNotApprovedException
import com.coradec.coradeck.ctrl.trouble.NoRouteForMessageException
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.LinkedBlockingDeque
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class BasicAgent : Logger(), Agent {
    private val routes = ConcurrentHashMap<Class<*>, (Any) -> Unit>()
    private val approvedCommands = CopyOnWriteArraySet(INTERNAL_COMMANDS)
    private val queue = LinkedBlockingDeque<Information>()
    override val queueSize: Int get() = queue.size

    override fun <I : Information> inject(message: I): I = message.also {
        if (message.urgent) queue.addFirst(message) else queue.addLast(message)
        EMS.execute(this)
    }

    fun inject(action: () -> Unit): ActionCommand = ActionCommand(caller, this, action).also {
        queue.addLast(it)
        EMS.execute(this)
    }

    override fun trigger() = onMessage(queue.take().also { it.dispatch() })
    override fun synchronize() {
        inject(Synchronization()).standBy()
    }

    override fun approve(vararg cmd: KClass<out Command>) {
        approvedCommands.addAll(cmd.toList())
    }

    override fun disapprove(vararg cmd: KClass<out Command>) {
        approvedCommands.removeAll(cmd.toList())
    }

    protected open fun onMessage(message: Information): Unit = when (message) {
        is Command ->
            if (approvedCommands.any { it.isInstance(message) })
                try { message.execute() } catch (e: Throwable) {
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
        in routes.keys -> with (routes.filterKeys { it.isInstance(message) }.iterator().next().value) {
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

    protected fun <T : Information> addRoute(type: Class<out T>, processor: (T) -> Unit) {
        inject(AddRouteCommand(caller, this, type, processor))
    }

    protected fun removeRoute(type: Class<out Information>) {
        inject(RemoveRouteCommand(caller, this, type))
    }

    private inner class AddRouteCommand<T>(
        origin: Origin,
        recipient: Recipient,
        private val type: Class<out Any>,
        private val processor: (T) -> Unit
    ) : BasicCommand(origin, recipient) {
        override fun execute() {
            routes[type] = processor as (Any) -> Unit
        }

    }

    private inner class RemoveRouteCommand(
        origin: Origin,
        recipient: Recipient,
        private val type: Class<out Any>
    ) : BasicCommand(origin, recipient) {
        override fun execute() {
            routes.remove(type)
        }

    }

    private inner class Synchronization : BasicRequest(here, this@BasicAgent)

    companion object {
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
