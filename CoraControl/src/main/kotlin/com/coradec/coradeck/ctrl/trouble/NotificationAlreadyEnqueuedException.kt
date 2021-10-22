/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.trouble

import com.coradec.coradeck.com.model.Notification
import com.coradec.coradeck.com.model.NotificationState
import java.util.*

class NotificationAlreadyEnqueuedException(val notification: Notification<*>, val states: EnumSet<NotificationState>) : ControlException()
