/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.trouble

import com.coradec.coradeck.com.model.Notification

class NotificationRejectedException(val notification: Notification<*>): CommunicationException()
