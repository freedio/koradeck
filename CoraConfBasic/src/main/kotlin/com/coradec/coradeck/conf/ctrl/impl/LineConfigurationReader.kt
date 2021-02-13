package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.ctrl.AdvancedLineParser
import com.coradec.coradeck.core.util.lines
import java.net.URL

abstract class LineConfigurationReader: BasicConfigurationReader() {
    protected abstract val parser: AdvancedLineParser
    override fun read(location: URL): Map<String, Any> {
        return parser.parse(location.lines)
    }
}
