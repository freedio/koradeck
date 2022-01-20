/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model

import kotlin.reflect.KClass
import kotlin.reflect.KType

interface Configuration {
    /** Retrieves the global property with the specified name, cast to the specified type, if present. */
    operator fun <P: Any> get(type: KClass<P>, name: String): P?
    /** Retrieves the global property with the specified name, cast to the specified type, if present. */
    operator fun <P: Any> get(type: KType, name: String): P?
}
