/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl

import java.net.URL
import java.nio.file.Path

interface ConfigurationReader {
    fun read(location: URL): Map<String, Any>
    fun read(path: Path): Map<String, Any>
}
