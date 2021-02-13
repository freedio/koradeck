/*
 * Copyright ⓒ 2019 by Coradec GmbH. All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin

interface StringLogEntry: LogEntry {

    companion object {
        operator fun invoke(origin: Origin, level: LogLevel, text: String, vararg args: Any): LogEntry =
                CoraCom.createStringLogEntry(origin, level, text, *args)
    }

}
