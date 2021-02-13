/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.session.model

import com.coradec.coradeck.core.ctrl.ThreadMonitor
import com.coradec.coradeck.core.util.whenTerminated
import com.coradec.coradeck.session.model.impl.BasicSession
import com.coradec.coradeck.session.model.impl.SecureSession
import com.coradec.coradeck.session.trouble.InvalidSessionTypeParameterException
import java.util.concurrent.ConcurrentHashMap

interface Session {
    val user: String
    val authenticated: Boolean

    companion object {
        val secureSessions = System.getProperty("cora.sessions")?.let {
            try {
                SessionType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                throw InvalidSessionTypeParameterException(it)
            }
        } ?: SessionType.standard
        val currentUser = System.getProperty("user.name")!!
        private val sessions = ConcurrentHashMap<Thread, Session>()
        val current: Session get() = sessions.computeIfAbsent(Thread.currentThread()) { thread ->
            new.also {
                thread.whenTerminated { sessions.remove(it) }
            }
        }
        val new: Session get() = BasicSession(currentUser)
        val secure: Session get() = SecureSession(currentUser)

        fun new(user: String) = BasicSession(user)
        fun secure(user: String) = SecureSession(user)
    }
}
