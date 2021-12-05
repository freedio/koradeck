/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.conf.model.LocalProperty
import com.coradec.coradeck.core.model.Deferred
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Prioritized
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.core.util.*
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.Duration
import java.time.ZonedDateTime
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
    override val validUpTo: ZonedDateTime = validFrom + PROP_VALIDITY.value
) : Logger(), Information {
    override val due: ZonedDateTime get() = validFrom
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

    override fun wrap(origin: Origin): Notification<*> =
        BasicNotification(this, origin, priority, createdAt, session, validFrom, validUpTo)

    override fun toString(): String =
        "%s(%s)".format(shortClassname, properties.formatted)

    override fun format(known: Set<Any?>): String =
        "%s(%s)".format(shortClassname, properties.formatted(known))

    override fun compareTo(other: Prioritized): Int = priority.compareTo(other.priority)
    override fun compareTo(other: Deferred): Int {
        TODO("Not yet implemented")
    }

    companion object {
        private val TEXT_ARGUMENT_NOT_FOUND = LocalText("ArgumentNotFound2")
        private val TEXT_INVALID_ARGUMENT = LocalText("InvalidArgument3")
        private val TEXT_PRIVATE_PROPERTY = LocalText("PrivateProperty2")

        val PROP_VALIDITY = LocalProperty("Validity", Duration.ofSeconds(20))
    }
}
