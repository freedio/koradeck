/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Information

interface EMS {
    fun execute(agent: Agent)
    fun inject(message: Information)
    fun post(obj: Any)
    fun onQueueEmpty(function: () -> Unit)
    fun standBy()
}
