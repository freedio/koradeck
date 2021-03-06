package com.coradec.coradeck.conf.module

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.conf.model.Configurations
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class CoraConfImpl: CoraConfAPI {
    override val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
    override val jsonMapper: ObjectMapper = ObjectMapper(JsonFactory())
            .registerKotlinModule()
    override val xmlMapper: ObjectMapper = XmlMapper()

    override fun getConfiguration(context: String): Configuration = Configurations.byContext(context)
}
