/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.com.model

import com.coradec.coradeck.com.module.CoraCom
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.text.model.Text

interface TextLogEntry: LogEntry {

    companion object {
        operator fun invoke(origin: Origin, level: LogLevel, template: Text, vararg args: Any): TextLogEntry =
                CoraCom.createTextLogEntry(origin, level, template, *args)

    }

}
