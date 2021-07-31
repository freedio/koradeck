package com.coradec.coradeck.ctrl.trouble

import com.coradec.coradeck.com.model.Command

class CommandNotApprovedException(val command: Command) : CoraControlException()
