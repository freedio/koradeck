/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.core.model.Deferred
import com.coradec.coradeck.core.model.Prioritized
import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.ctrl.module.CoraControl

interface Task: Runnable, Prioritized, Deferred {
    companion object {
        operator fun invoke(executable: Runnable, prio: Priority = defaultPriority) = CoraControl.taskOf(executable, prio)
    }
}
