/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.type.module.CoraType
import kotlin.reflect.KClass
import kotlin.reflect.KType

class SystemConfiguration: Configuration {
    override fun <P : Any> get(type: KClass<P>, name: String): P? = CoraType.castTo(System.getProperty(name), type)
    override fun <P : Any> get(type: KType, name: String): P? = CoraType.castTo(System.getProperty(name), type)
}
