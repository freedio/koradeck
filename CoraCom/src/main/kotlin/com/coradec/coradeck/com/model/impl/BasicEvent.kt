package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Event
import com.coradec.coradeck.core.model.Origin
import java.time.ZonedDateTime

open class BasicEvent(origin: Origin, created: ZonedDateTime = ZonedDateTime.now()) : BasicInformation(origin, created), Event
