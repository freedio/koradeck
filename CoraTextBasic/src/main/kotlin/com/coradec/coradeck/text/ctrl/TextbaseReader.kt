/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.ctrl

import com.coradec.coradeck.conf.ctrl.AdvancedLineParser
import com.coradec.coradeck.core.util.lines
import java.net.URL

object TextbaseReader {
    val parser = AdvancedLineParser(
        delimiters = "=:",
        escape = '\\',
        comments = "#",
        beginquotes = "\"'",
        endquotes = "\"'"
    )
    fun read(location: URL): Map<String, String> {
        return parser.parse(location.lines)
    }
}
