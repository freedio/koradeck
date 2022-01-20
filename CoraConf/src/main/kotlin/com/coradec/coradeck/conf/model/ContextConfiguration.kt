/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model

import kotlin.reflect.KClass
import kotlin.reflect.KType

interface ContextConfiguration : Configuration {
    /** Retrieves the property with the specified name in the specified context (if any), cast to the specified type, if present. */
    operator fun <P: Any> get(type: KClass<P>, name: String, context: String? = null): P?
    /** Retrieves the property with the specified name in the specified context (if any), cast to the specified type, if present. */
    operator fun <P: Any> get(type: KType, name: String, context: String? = null): P?
    /** Retrieves the global property with the specified name, cast to the specified type, if present. */
    override operator fun <P: Any> get(type: KClass<P>, name: String): P? = get(type, name, null)
    /** Retrieves the global property with the specified name, cast to the specified type, if present. */
    override operator fun <P: Any> get(type: KType, name: String): P? = get(type, name, null)
}
