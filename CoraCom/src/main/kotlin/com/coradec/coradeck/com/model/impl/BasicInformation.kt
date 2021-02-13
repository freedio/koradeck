package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.core.model.Expiration
import com.coradec.coradeck.core.model.Expiration.Companion.never_expires
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.session.model.Session
import java.time.ZonedDateTime

open class BasicInformation(
        override val origin: Origin,
        override val createdAt: ZonedDateTime = ZonedDateTime.now(),
        override val session: Session = Session.current,
        override val expires: Expiration = never_expires
) : Information {
    override val urgent: Boolean = false
}
