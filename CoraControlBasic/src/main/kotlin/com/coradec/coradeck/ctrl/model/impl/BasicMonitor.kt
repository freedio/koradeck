/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.com.model.*
import com.coradec.coradeck.com.trouble.*
import com.coradec.coradeck.core.util.relax
import com.coradec.coradeck.ctrl.model.Monitor
import com.coradec.coradeck.text.model.LocalText
import java.util.concurrent.Semaphore

class BasicMonitor : Logger(), Monitor {
    private val notifications = mutableSetOf<Notification<*>>()
    private val latch = Semaphore(1)
    override fun <I : Information> register(notification: Notification<I>) {
        synchronized(notifications) {
            if (notifications.isEmpty()) latch.acquire()
            notifications.add(notification)
        }
        if (notification.content is Request) (notification.content as Request).whenFinished { checkout(this, notification) }
        else notification.whenFinished { checkout(this) }
    }

    override fun onClear(action: () -> Unit) {
        latch.acquire()
        try {
            action.invoke()
        } catch(e: Exception) {
            error(e)
        } finally {
            latch.release()
        }
    }

    private fun <I: Information> checkout(request: Request, notification: Notification<I>) {
        when (request.state) {
            RequestState.FAILED -> error(RequestFailedException(request))
            RequestState.CANCELLED -> error(RequestCancelledException(request))
            RequestState.LOST -> error(RequestLostException(request))
            else -> relax()
        }
        synchronized(notifications) {
            if (!notifications.remove(notification)) warn(TEXT_REQUEST_NOT_MONITORED, request)
            else if (notifications.isEmpty()) latch.release()
        }
    }

    private fun <I: Information> checkout(notification: Notification<I>) {
        when (notification.state) {
            NotificationState.CRASHED -> error(NotificationCrashedException(notification))
            NotificationState.REJECTED -> error(NotificationRejectedException(notification))
            NotificationState.LOST -> error(NotificationLostException(notification))
            else -> relax()
        }
        synchronized(notifications) {
            if (!notifications.remove(notification)) warn(TEXT_NOTIFICATION_NOT_MONITORED, notification)
            else if (notifications.isEmpty()) latch.release()
        }
    }

    companion object {
        private val TEXT_REQUEST_NOT_MONITORED = LocalText("RequestNotMonitored1")
        private val TEXT_NOTIFICATION_NOT_MONITORED = LocalText("NotificationNotMonitored1")
    }
}
