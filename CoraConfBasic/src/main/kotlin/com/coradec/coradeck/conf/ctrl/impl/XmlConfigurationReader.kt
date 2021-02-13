/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.module.CoraConf
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL

object XmlConfigurationReader : BasicConfigurationReader() {
    override fun read(location: URL): Map<String, Any> {
        TODO("Not yet implemented")
    }
}
