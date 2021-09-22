/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.ctrl.AdvancedLineParser

object PropConfigurationReader: LineConfigurationReader() {
    override val parser = AdvancedLineParser(
            delimiters = "=:",
            escape = '\\',
            comments = "#!",
            beginquotes = "\"'",
            endquotes = "\"'"
    )
}
