/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.module

import com.coradec.coradeck.conf.model.ContextConfiguration
import com.coradec.coradeck.module.model.CoraModuleAPI
import com.fasterxml.jackson.databind.ObjectMapper

interface CoraConfAPI: CoraModuleAPI {
    /** The system wide YAML parser. */
    val yamlMapper: ObjectMapper
    /** The system wide JSON parser. */
    val jsonMapper: ObjectMapper
    /** The system wide XML parser. */
    val xmlMapper: ObjectMapper
    /** The central configuration. */
    val config: ContextConfiguration
}
