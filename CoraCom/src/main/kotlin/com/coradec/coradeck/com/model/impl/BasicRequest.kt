package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.core.model.Origin

open class BasicRequest(origin: Origin, recipient: Recipient): BasicMessage(origin, recipient), Request
