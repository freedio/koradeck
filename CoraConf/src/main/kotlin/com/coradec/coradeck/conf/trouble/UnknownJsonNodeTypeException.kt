package com.coradec.coradeck.conf.trouble

import com.fasterxml.jackson.databind.JsonNode

class UnknownJsonNodeTypeException(val node: JsonNode) : ConfigurationException() {

}
