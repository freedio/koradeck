/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.conf.trouble.PropertyUndefinedException
import kotlin.reflect.KType

class MandatoryContextProperty<P:Any>(type: KType, val context: String, name: String) : NamedProperty<P>(name, type) {
    private val config = Configuration(context)
    override val value: P get() = config[type, name] ?: throw PropertyUndefinedException("$context.$name", type)
}
