/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Property
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType

abstract class BasicProperty<P: Any>(val type: KType) : Property<P> {
    constructor(type: KClass<P>): this(type.createType(nullable = false))
}
