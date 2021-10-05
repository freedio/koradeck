/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Information.Companion.LOST_ITEMS
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.com.model.State.*
import com.coradec.coradeck.com.model.StateObserver
import com.coradec.coradeck.core.model.Deferred
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Prioritized
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.util.*
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import java.util.Collections.binarySearch
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility.PRIVATE
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Suppress("UNCHECKED_CAST")
open class BasicInformation(
    override val origin: Origin,
    override val priority: Priority = defaultPriority,
    override val createdAt: ZonedDateTime = ZonedDateTime.now(),
    override val session: Session = Session.current,
    override val validFrom: ZonedDateTime = createdAt,
    override val validUpTo: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC)
) : Logger(), Information {
    private val stateRegistry = CopyOnWriteArraySet<Observer>()
    private val myStates = ArrayList<State>().apply { add(NEW) }
    override val due: ZonedDateTime get() = validFrom
    override val states: List<State> = Collections.unmodifiableList(myStates)
    override val new: Boolean get() = states.singleOrNull() == NEW
    override val enqueued: Boolean get() = ENQUEUED in states
    override val dispatched: Boolean get() = DISPATCHED in states
    override val delivered: Boolean get() = DELIVERED in states
    override val processed: Boolean get() = PROCESSED in states
    override val observerCount: Int get() = stateRegistry.size
    override fun <I : Information> copy(vararg substitute: Pair<String, Any?>): I = copy(substitute.toMap())
    override fun <I : Information> copy(substitute: Map<String, Any?>): I {
        val dynamic = listOf("createdAt")
        val klass = this::class as KClass<I>
        val primaryConstructor = klass.primaryConstructor ?: throw IllegalStateException("Can't copy a $classname")
        val parameters = primaryConstructor.parameters
            .associateBy { para -> para.name }
            .removeNullKeys()
            .filterKeys { it !in dynamic }

        val args = klass.memberProperties
            .filter { prop ->
                (prop.visibility != PRIVATE).also { if (!it) warn(TEXT_PRIVATE_PROPERTY, prop.name, classname) }
            }
            .associate { prop -> Pair(parameters[prop.name], prop.getter.call(this)) }
            .removeNullKeys()
            .let { map ->
                if (substitute.isNotEmpty()) {
                    val mutableMap = map.toMutableMap()
                    substitute
                        .filter { (key, value) ->
                            val found = key in parameters
                            val valid = found && (parameters[key]?.type?.classifier as? KClass<*>)?.isInstance(value) ?: false
                            if (!found) warn(TEXT_ARGUMENT_NOT_FOUND, key, klass.classname)
                            else if (!valid) warn(
                                TEXT_INVALID_ARGUMENT,
                                key,
                                klass.classname,
                                parameters[key]?.type?.name ?: "unknown"
                            )
                            found && valid
                        }
                        .filterKeys { key ->
                            key in parameters && key !in dynamic
                        }
                        .forEach { (name, value) ->
                            mutableMap[parameters[name]!!] = value
                        }
                    mutableMap
                } else map
            }
        return primaryConstructor.callBy(args)
    }

    override var state: State
        get() = synchronized(myStates) { myStates.last() }
        set(state) {
            interceptSetState(state)
            synchronized(myStates) {
                fun invert(i: Int): Int = if (i < 0) -i - 1 else i
                fun addState(newstate: State) {
                    myStates.add(invert(binarySearch(myStates, newstate)), newstate)
                }
                if (state !in myStates) {
                    val event = StateChangedEvent(here, this, myStates.last(), state.apply { addState(this) })
                    stateRegistry.forEach { if (it.notify(event)) stateRegistry.remove(it) }
                }
            }
        }

    protected open fun interceptSetState(state: State) = relax()
    override fun enregister(observer: Observer) = stateRegistry.add(observer)
    override fun deregister(observer: Observer) = stateRegistry.remove(observer)
    override fun enqueue() = also { state = ENQUEUED }
    override fun dispatch() = also { state = DISPATCHED }
    override fun deliver() = also { state = DELIVERED }
    override fun process() = also { state = PROCESSED }
    override fun miss() = also {
        state = LOST
        LOST_ITEMS += this
    }

    override fun toString(): String =
        "%s(%s)".format(shortClassname, properties.formatted)

    override fun format(known: Set<Any?>): String =
        "%s(%s)".format(shortClassname, properties.formatted(known))

    override fun compareTo(other: Prioritized): Int = priority.compareTo(other.priority)
    override fun compareTo(other: Deferred): Int {
        TODO("Not yet implemented")
    }

    override fun whenState(state: State, action: () -> Unit) {
        stateRegistry.add(StateObserver(state, action))
        synchronized(myStates) {
            if (state in myStates) {
                val event = StateChangedEvent(here, this, myStates.dropLast(1).last(), myStates.last())
                stateRegistry.forEach { if (it.notify(event)) stateRegistry.remove(it) }
            }
        }
    }

    companion object {
        private val TEXT_ARGUMENT_NOT_FOUND = LocalText("ArgumentNotFound2")
        private val TEXT_INVALID_ARGUMENT = LocalText("InvalidArgument3")
        private val TEXT_PRIVATE_PROPERTY = LocalText("PrivateProperty2")
    }
}
