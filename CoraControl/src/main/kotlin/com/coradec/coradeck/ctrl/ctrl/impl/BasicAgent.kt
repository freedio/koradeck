/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.impl.ActionCommand
import com.coradec.coradeck.com.model.impl.BasicCommand
import com.coradec.coradeck.com.model.impl.DummyRequest
import com.coradec.coradeck.com.trouble.NotificationRejectedException
import com.coradec.coradeck.core.util.caller
import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.module.CoraControl.IMMEX
import com.coradec.coradeck.ctrl.trouble.CommandNotApprovedException
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass

@Suppress("LeakingThis")
open class BasicAgent() : Logger(), Agent {
    private val index = NEXT.getAndIncrement()
    private val routes = ConcurrentHashMap<Class<*>, (Any) -> Unit>()
    private val approvedCommands = CopyOnWriteArraySet(INTERNAL_COMMANDS)
    private val shutdownActions = mutableListOf<() -> Unit>()
    override val representation: String get() = "BasicAgent#$index"

    init {
        AGENTS[index] = this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <I : Information> accept(info: I): Message<I> = IMMEX.inject(
        when {
            info is Message<*> && info.recipient == this -> info as Message<I>
            info is Notification<*> -> (info unto this) as Message<I>
            else -> info unto this
        }
    )

    override fun synchronize() {
        val sync = Semaphore(0)
        accept(Synchronization(sync))
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

    override fun receive(notification: Notification<*>): Unit = when (val content = notification.content) {
        is Command ->
            if (approvedCommands.any { it.isInstance(content) }) try {
                content.execute()
            } catch (e: Throwable) {
                error(e, TEXT_COMMAND_FAILED, content::class.classname, e.toString())
                notification.crash(e)
            } else {
                notification.crash(CommandNotApprovedException(content))
                error(TEXT_MESSAGE_NOT_APPROVED, content.classname, content)
            }
        is Synchronization -> content.succeed().also { debug("Synchronization point «%s» reached", content) }
        in routes.keys -> with(routes.filterKeys { it.isInstance(content) }.iterator().next().value) {
            try {
                invoke(content)
            } catch (e: Throwable) {
                error(e, TEXT_MESSAGE_FAILED, content.classname, e.toString())
                notification.crash(e)
            }
        }
        is DummyRequest -> content.succeed().also { debug("DummyRequest processed.") }
        else -> {
            error(TEXT_MESSAGE_NOT_UNDERSTOOD, content.classname, content)
            throw NotificationRejectedException(notification)
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Information> route(type: Class<out T>, processor: (T) -> Unit) {
        routes[type] = processor as (Any) -> Unit
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Information> route(type: KClass<out T>, processor: (T) -> Unit) {
        routes[type.java] = processor as (Any) -> Unit
    }

    protected fun unroute(type: Class<out Information>) {
        routes -= type
    }

    protected fun unroute(type: KClass<out Information>) {
        routes -= type.java
    }

    class Synchronization(val sync: Semaphore) : BasicCommand(caller) {
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
