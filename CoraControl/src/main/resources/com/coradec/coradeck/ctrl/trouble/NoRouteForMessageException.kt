package com.coradec.coradeck.ctrl.trouble

import com.coradec.coradeck.com.model.Information

class NoRouteForMessageException(val info: Information) : CoraControlException()
