/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.model.RequestState.*
import com.coradec.coradeck.com.model.RequestState.Companion.FINISHED
import com.coradec.coradeck.com.model.impl.BasicRequest
import com.coradec.coradeck.com.model.impl.RequestStateChangedEvent
import com.coradec.coradeck.com.model.impl.StateChangedEvent
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.ctrl.Agent
import com.coradec.coradeck.ctrl.model.ItemList
import com.coradec.coradeck.ctrl.module.CoraControl.IMMEX
import com.coradec.coradeck.ctrl.trouble.LostInformationException
import com.coradec.coradeck.session.model.Session
import com.coradec.coradeck.text.model.LocalText
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

class BasicItemList(
    origin: Origin,
    private val items: Iterator<Information>,
    priority: Priority = Priority.defaultPriority,
    createdAt: ZonedDateTime = ZonedDateTime.now(),
    session: Session = Session.current,
    validFrom: ZonedDateTime = createdAt,
    validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC),
    private val processor: Agent? = null
) : BasicRequest(origin, priority, createdAt, session, validFrom, validUpto), ItemList {
    constructor(
        origin: Origin,
        items: Sequence<Information>,
        priority: Priority = Priority.defaultPriority,
        createdAt: ZonedDateTime = ZonedDateTime.now(),
        session: Session = Session.current,
        validFrom: ZonedDateTime = createdAt,
        validUpto: ZonedDateTime = ZonedDateTime.of(LocalDateTime.MAX, ZoneOffset.UTC),
        processor: Agent? = null
    ) : this(origin, items.iterator(), priority, createdAt, session, validFrom, validUpto, processor)

    private val actionItems = Sequence { items }.map {
        if (it is Notification<*>) it else Notification(it)
    }.iterator()

    override fun execute(): Unit = when {
        complete -> relax()
        actionItems.hasNext() -> actionItems.next().let { item ->
            if (item.content is Request) (item.content as Request).enregister(this) else item.enregister(this)
            if (!item.complete) {
                if (!item.enqueued) processor?.accept(item) ?: IMMEX.inject(item)
            } else process(item, item.state)
        }
        else -> succeed()
    }

    override fun onNotification(event: Event): Boolean = when {
        complete -> false
        event is RequestStateChangedEvent -> {
            val element: Request = event.message
            trace("Request state changed: %s %s→%s", element, event.previous, event.current)
            val newState: RequestState = event.current
            process(element, newState)
            newState in FINISHED
        }
        event is StateChangedEvent -> {
            val element: Notification<*> = event.message
            trace("Notification state changed: %s %s→%s", element, event.previous, event.current)
            val newState: NotificationState = event.current
            process(element, newState)
            newState == NotificationState.PROCESSED
        }
        else -> false.also { warn(TEXT_EVENT_NOT_UNDERSTOOD, event) }
    }

    private fun process(item: Notification<*>, state: NotificationState) {
        when (val element: Information = item.content) {
            is Request -> process(element, element.state)
            is Notification<*> -> process(element, element.state)
            else -> process(item, element, state)
        }
    }

    private fun process(notification: Notification<out Information>, element: Information, state: NotificationState) = when (state) {
        NotificationState.PROCESSED -> execute()
        NotificationState.REJECTED, NotificationState.CRASHED -> fail(notification.problem)
        NotificationState.LOST -> fail(LostInformationException(element))
        else -> relax()
    }

    private fun process(element: Request, state: RequestState) = when (state) {
        SUCCESSFUL -> execute()
        FAILED -> this@BasicItemList.fail(element.reason)
        CANCELLED -> this@BasicItemList.cancel()
        else -> relax()
    }

    companion object {
        private val TEXT_EVENT_NOT_UNDERSTOOD = LocalText("EventNotUnderstood1")
    }
}
