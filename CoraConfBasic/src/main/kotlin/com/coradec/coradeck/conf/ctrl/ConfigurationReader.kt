/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl

import java.net.URL

interface ConfigurationReader {
    fun read(location: URL): Map<String, Any>
}
