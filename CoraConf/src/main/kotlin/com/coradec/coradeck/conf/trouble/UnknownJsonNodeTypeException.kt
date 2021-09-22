/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.trouble

import com.fasterxml.jackson.databind.JsonNode

class UnknownJsonNodeTypeException(val node: JsonNode) : ConfigurationException() {

}
