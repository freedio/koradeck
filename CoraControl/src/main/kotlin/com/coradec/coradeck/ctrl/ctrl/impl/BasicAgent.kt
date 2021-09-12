package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.impl.ActionCommand
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.core.util.caller
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl.IMMEX
import com.coradec.coradeck.ctrl.trouble.CommandNotApprovedException
import com.coradec.coradeck.ctrl.trouble.NoRouteForMessageException
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "LeakingThis")
open class BasicAgent(override val capacity: Int = 1024) : Logger(), Agent {
    private val index = NEXT.getAndIncrement()
    private val routes = ConcurrentHashMap<Class<*>, (Any) -> Unit>()
    private val approvedCommands = CopyOnWriteArraySet(INTERNAL_COMMANDS)
    private val shutdownActions = mutableListOf<() -> Unit>()
    override val representation: String get() = "BasicAgent#$index"

    init {
        AGENTS[index] = this
    }

    override fun <M : Message> inject(message: M): M = IMMEX.inject(message.withDefaultRecipient(this)) as M

    override fun synchronize() {
        val sync = Semaphore(0)
        IMMEX.inject(Synchronization(sync))
        sync.acquire()
    }

    protected fun approve(vararg cmd: KClass<out Command>) {
        approvedCommands.addAll(cmd.toList())
    }

    protected fun disapprove(vararg cmd: KClass<out Command>) {
        approvedCommands.removeAll(cmd.toList())
    }

    protected fun atEnd(action: () -> Unit) = synchronized(shutdownActions) {
        if (shutdownActions.isEmpty()) Runtime.getRuntime().addShutdownHook(object : Thread("Finalization of $representation") {
            override fun run() {
                shutdownActions.forEach { it.invoke() }
            }
        })
        shutdownActions += action
    }

    override fun onMessage(message: Information) {
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
                    error(TEXT_MESSAGE_NOT_APPROVED, message.classname, message)
                }
            is Synchronization -> {
                debug("Synchronization point «%s» reached", message)
                message.succeed()
            }
            in routes.keys -> with(routes.filterKeys { it.isInstance(message) }.iterator().next().value) {
                try {
                    invoke(message)
                } catch (e: Throwable) {
                    error(e, TEXT_MESSAGE_FAILED, message.classname, e.toString())
                    if (message is Request) message.fail(e)
                }
            }
            else -> {
                if (message is Request) message.fail(NoRouteForMessageException(message))
                error(TEXT_MESSAGE_NOT_UNDERSTOOD, message.classname, message)
            }
        }
    }

    protected fun <T : Information> addRoute(type: Class<out T>, processor: (T) -> Unit) {
        routes[type] = processor as (Any) -> Unit
    }

    protected fun <T : Information> addRoute(type: KClass<out T>, processor: (T) -> Unit) {
        routes[type.java] = processor as (Any) -> Unit
    }

    protected fun removeRoute(type: Class<out Information>) {
        routes -= type
    }

    protected fun removeRoute(type: KClass<out Information>) {
        routes -= type.java
    }

    private inner class Synchronization(val sync: Semaphore, target: Recipient? = this@BasicAgent) : BasicCommand(caller, target = target) {
        override fun execute() {
            debug("Synchronization point reached")
            sync.release()
            succeed()
        }
    }

    companion object {
        private val AGENTS = ConcurrentHashMap<Int, Agent>()
        private val NEXT = AtomicInteger(0)
        private val TEXT_MESSAGE_NOT_UNDERSTOOD = LocalText("MessageNotUnderstood2")
        private val TEXT_MESSAGE_NOT_APPROVED = LocalText("MessageNotApproved2")
        private val TEXT_COMMAND_FAILED = LocalText("CommandFailed2")
        private val TEXT_MESSAGE_FAILED = LocalText("MessageFailed2")
        private val INTERNAL_COMMANDS = listOf(
            MultiRequest::class,
            ActionCommand::class,
            Synchronization::class
        )
    }
}
