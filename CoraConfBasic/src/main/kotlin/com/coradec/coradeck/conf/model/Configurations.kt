/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model

import com.coradec.coradeck.conf.ctrl.ConfigurationReader
import com.coradec.coradeck.conf.model.impl.*
import com.coradec.coradeck.conf.module.CoraConf
import com.coradec.coradeck.conf.trouble.ConfigurationNotFoundException
import com.coradec.coradeck.core.util.resource
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.reflect.KClass
import kotlin.reflect.KType

object Configurations : ContextConfiguration {
    private val sysconf by lazy { SystemConfiguration() }
    private val usrconf by lazy { UserConfiguration() }
    private val appconf by lazy { ApplicationConfiguration() }
    private val dflconf by lazy { DefaultConfiguration() }
    private val modconf by lazy { ModuleConfigurations() }
    private val ctrconf =
        CoraConf.yamlMapper.readValue<ConfigurationsConfig>(resource(Configurations::class, ".yaml").location)
    internal val readersByType: Map<String, ConfigurationReader> =
        ctrconf.configurationReaders.mapValues { (_, klass) -> klass.kotlin.objectInstance as ConfigurationReader }

    override fun <P : Any> get(type: KClass<P>, name: String, context: String?): P? =
        if (context != null) {
            val qn = "$context.$name"
            sysconf[type, qn] ?: usrconf[type, qn] ?: modconf[type, name, context] ?: appconf[type, qn] ?: dflconf[type, qn]
        }
        else sysconf[type, name] ?: usrconf[type, name] ?: appconf[type, name] ?: dflconf[type, name]

    override fun <P : Any> get(type: KType, name: String, context: String?): P? =
        if (context != null) {
            val qn = "$context.$name"
            sysconf[type, qn] ?: usrconf[type, qn] ?: modconf[type, name, context] ?: appconf[type, qn] ?: dflconf[type, qn]
        }
        else sysconf[type, name] ?: usrconf[type, name] ?: appconf[type, name] ?: dflconf[type, name]

    internal fun loadGlobalConfiguration(path: Path): Map<String, Any> {
        for ((type, reader) in readersByType) {
            val confPath = Paths.get("$path.$type")
            if (confPath.exists()) {
                return reader.read(confPath)
            }
        }
        throw ConfigurationNotFoundException(path.toString())
    }
}
