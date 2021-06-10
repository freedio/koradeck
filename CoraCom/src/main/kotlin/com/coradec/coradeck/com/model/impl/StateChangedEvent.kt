package com.coradec.coradeck.com.model.impl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.State
import com.coradec.coradeck.core.model.Origin

class StateChangedEvent(origin: Origin, val source: Information, val previous: State, val current: State) : BasicEvent(origin)
