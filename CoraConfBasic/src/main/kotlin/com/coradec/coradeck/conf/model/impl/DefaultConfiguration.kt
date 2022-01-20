/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.conf.model.Configurations
import com.coradec.coradeck.conf.trouble.ConfigurationNotFoundException
import com.coradec.coradeck.core.util.CONFIG_DIR
import com.coradec.coradeck.type.module.CoraType
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType

class DefaultConfiguration: Configuration {
    private val globalconfig = ConcurrentHashMap(
        try {
            Configurations.loadGlobalConfiguration(Paths.get(CONFIG_DIR).resolve("global-conf"))
                .mapValues { (_, value) -> value.toString() }
        } catch (e: ConfigurationNotFoundException) {
            emptyMap()
        }
    )

    override fun <P : Any> get(type: KClass<P>, name: String): P? = CoraType.castTo(globalconfig[name], type)
    override fun <P : Any> get(type: KType, name: String): P? = CoraType.castTo(globalconfig[name], type)
}
