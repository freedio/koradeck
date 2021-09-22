/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model.impl

import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.core.model.Priority.Companion.defaultPriority
import com.coradec.coradeck.ctrl.model.Task
import java.time.ZonedDateTime

class BasicTask(
    private val executable: Runnable,
    override val priority: Priority = defaultPriority,
    override val due: ZonedDateTime = ZonedDateTime.now()
) : Task {
    override fun run() = executable.run()

}
