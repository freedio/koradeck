/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Property
import com.coradec.coradeck.conf.module.CoraConf
import kotlin.reflect.KType
import kotlin.reflect.typeOf

open class ContextProperty<P: Any>(
        type: KType,
        val context: String,
        name: String,
        default: P
): DefaultNamedProperty<P>(name, type, default) {
    override val value: P get() = CoraConf.config[type, name, context] ?: super.value

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        inline operator fun <reified P: Any> invoke(context: String, name: String, default: P): Property<P> =
            ContextProperty(typeOf<P?>(), context, name, default)
        inline operator fun <reified P: Any> invoke(context: String, name: String): Property<P> =
            MandatoryContextProperty(typeOf<P?>(), context, name)
    }
}
