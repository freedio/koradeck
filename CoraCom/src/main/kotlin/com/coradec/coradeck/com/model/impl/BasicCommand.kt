package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Command
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin

abstract class BasicCommand(origin: Origin, recipient: Recipient): BasicRequest(origin, recipient), Command
