/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.ctrl.impl

import com.coradec.coradeck.conf.module.CoraConf
import com.fasterxml.jackson.databind.ObjectMapper

object YamlConfigurationReader : JacksonConfigurationReader() {
    override val mapper: ObjectMapper = CoraConf.yamlMapper
}
