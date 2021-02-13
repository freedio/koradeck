/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

interface Information {
    val origin: Origin
    val session: Session
    val createdAt: ZonedDateTime
    val urgent: Boolean
    val expires: Expiration
}
