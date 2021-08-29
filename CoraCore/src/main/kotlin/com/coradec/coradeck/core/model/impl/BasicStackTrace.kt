package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.model.StackTrace

class BasicStackTrace(val frames: List<StackFrame>): StackTrace
