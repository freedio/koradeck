/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.module.CoraConf
import com.fasterxml.jackson.databind.ObjectMapper

object JsonConfigurationReader: JacksonConfigurationReader() {
    override val mapper: ObjectMapper = CoraConf.jsonMapper
}
