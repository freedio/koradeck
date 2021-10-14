/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.trouble

import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.State
import java.util.*

class NotificationAlreadyEnqueuedException(val notification: Notification<*>, val states: EnumSet<State>) : ControlException()
