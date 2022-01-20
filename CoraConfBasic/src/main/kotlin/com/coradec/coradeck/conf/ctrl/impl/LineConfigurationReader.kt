/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.ctrl.AdvancedLineParser
import com.coradec.coradeck.core.util.lines
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.readLines

abstract class LineConfigurationReader: BasicConfigurationReader() {
    protected abstract val parser: AdvancedLineParser
    override fun read(location: URL): Map<String, Any> = parser.parse(location.lines)
    override fun read(path: Path): Map<String, Any> = parser.parse(path.readLines())
}
