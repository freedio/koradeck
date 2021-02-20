package com.coradec.coradeck.conf.model.impl

import kotlin.reflect.KType

open class DefaultNamedProperty<P: Any>(
        name: String,
        type: KType,
        default: P
): NamedProperty<P>(name, type) {
    override val value: P = default
}
