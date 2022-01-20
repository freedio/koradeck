/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.conf.model.Configurations
import com.coradec.coradeck.conf.model.ContextConfiguration
import com.coradec.coradeck.conf.trouble.ContextConfigurationNotFoundException
import com.coradec.coradeck.core.model.ClassPathResource
import com.coradec.coradeck.core.util.classname
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ModuleConfigurations : ContextConfiguration {
    private val configurations = ConcurrentHashMap<String, Configuration>()

    override fun <P : Any> get(type: KClass<P>, name: String, context: String?): P? =
        if (context == null) throw IllegalArgumentException("Missing context for property «$name» with type «${type.classname}")
        else configurations.computeIfAbsent(context) { loadContext(context) }[type, name]

    override fun <P : Any> get(type: KType, name: String, context: String?): P? =
        if (context == null) throw IllegalArgumentException("Missing context for property «$name» with type «${type.classname}")
        else configurations.computeIfAbsent(context) { loadContext(context) }[type, name]

    private fun loadContext(context: String): Configuration {
        val ctxt = context.replace('.', '/')
        for ((type, reader) in Configurations.readersByType) {
            val resource = ClassPathResource("$ctxt$type")
            if (resource.exists) return reader.read(resource.location).toConfiguration()
        }
        throw ContextConfigurationNotFoundException(context)
    }

    private fun Map<String, Any>.toConfiguration(): Configuration = MappedConfiguration(this)
}
