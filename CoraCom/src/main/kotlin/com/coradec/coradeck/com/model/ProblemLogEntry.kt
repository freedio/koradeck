/*
 * Copyright ⓒ 2019 by Coradec GmbH. All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.text.model.Text

interface ProblemLogEntry: LogEntry {
    val problem: Throwable

    companion object {
        operator fun invoke(origin: Origin, level: LogLevel, problem: Throwable, template: Text? = null, vararg args: Any) =
                CoraCom.createProblemLogEntry(origin, level, problem, template, *args)
    }

}
