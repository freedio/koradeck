/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.conf.model.Configurations
import com.coradec.coradeck.conf.trouble.ConfigurationNotFoundException
import com.coradec.coradeck.core.util.APPLICATION
import com.coradec.coradeck.core.util.USER_HOME
import com.coradec.coradeck.type.module.CoraType
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType

class ApplicationConfiguration : Configuration {
    private val appconfig = ConcurrentHashMap(
        try {
            if (APPLICATION.isNotBlank())
                Configurations.loadGlobalConfiguration(Paths.get(USER_HOME).resolve(".coradec").resolve(APPLICATION))
                    .mapValues { (_, value) -> value.toString() }
            else emptyMap()
        } catch (e: ConfigurationNotFoundException) {
            emptyMap()
        }
    )

    override fun <P : Any> get(type: KClass<P>, name: String): P? = CoraType.castTo(appconfig[name], type)
    override fun <P : Any> get(type: KType, name: String): P? = CoraType.castTo(appconfig[name], type)
}
