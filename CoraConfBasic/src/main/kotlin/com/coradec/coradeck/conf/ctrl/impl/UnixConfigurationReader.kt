/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.ctrl.AdvancedLineParser

object UnixConfigurationReader: LineConfigurationReader() {
    override val parser = AdvancedLineParser(
            delimiters = "=:",
            escape = '\\',
            comments = "#!",
            beginquotes = "\"'",
            endquotes = "\"'"
    )
}
