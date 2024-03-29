/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.trouble

import com.coradec.coradeck.com.model.Information

class NoRouteForMessageException(val info: Information) : ControlException()
