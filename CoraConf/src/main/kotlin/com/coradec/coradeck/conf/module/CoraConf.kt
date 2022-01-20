/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.module

import com.coradec.coradeck.conf.model.ContextConfiguration
import com.coradec.coradeck.module.model.CoraModule
import com.fasterxml.jackson.databind.ObjectMapper

object CoraConf: CoraModule<CoraConfAPI>() {
    /** The system wide YAML parser. */
    val yamlMapper: ObjectMapper get() = impl.yamlMapper
    /** The system wide JSON parser. */
    val jsonMapper: ObjectMapper get() = impl.jsonMapper
    /** The system wide XML parser. */
    val xmlMapper: ObjectMapper get() = impl.xmlMapper
    /** The central configuration. */
    val config: ContextConfiguration get() = impl.config
}
