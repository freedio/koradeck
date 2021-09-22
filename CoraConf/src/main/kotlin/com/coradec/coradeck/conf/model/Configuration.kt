/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model

import com.coradec.coradeck.conf.module.CoraConf
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface Configuration {
    /** Retrieves the property with the specified name, cast to the specified type, or `null` if there is none such. */
    operator fun <P: Any> get(type: KClass<P>, name: String): P?
    /** Retrieves the property with the specified name, cast to the specified type, or `null` if there is none such. */
    operator fun <P: Any> get(type: KType, name: String): P?

    companion object {
        operator fun invoke(context: String) = CoraConf.getConfiguration(context)
    }
}
