package com.coradec.coradeck.conf.model

import com.coradec.coradeck.com.ctrl.impl.Logger
import com.coradec.coradeck.conf.ctrl.ConfigurationReader
import com.coradec.coradeck.conf.model.impl.MappedConfiguration
import com.coradec.coradeck.conf.module.CoraConf
import com.coradec.coradeck.conf.trouble.ContextConfigurationNotFoundException
import com.coradec.coradeck.core.model.ClassPathResource
import com.coradec.coradeck.core.util.resource
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

object Configurations : Logger() {
    private val allProperties = ConcurrentHashMap<String, Any>()
    private val loadedContexts = CopyOnWriteArraySet<String>()
    private val configuration =
        CoraConf.yamlMapper.readValue<ConfigurationsConfig>(resource(Configurations::class, ".yaml").location)
    private val readersByType: Map<String, ConfigurationReader> =
        configuration.configurationReaders.mapValues { (_, klass) -> klass.kotlin.objectInstance as ConfigurationReader }

    fun byContext(context: String): Configuration {
        if (loadedContexts.add(context)) loadContext(context)
        return allProperties
            .filter { it.key.startsWith(context) }
            .mapKeys { (key, _) -> key.substring(context.length + 1) }
            .toConfiguration()
    }

    private fun loadContext(context: String) {
        debug("Loading configuration context «%s»", context)
        val ctxt = context.replace('.', '/')
        for ((type, reader) in readersByType) {
            if (ClassPathResource("$ctxt$type").ifExists {
                    allProperties.putAll(reader.read(location).mapKeys { (key, _) -> "$context.$key" })
                }
            ) return
        }
        throw ContextConfigurationNotFoundException(context)
    }
}

private fun Map<String, Any>.toConfiguration(): Configuration = MappedConfiguration(this)
