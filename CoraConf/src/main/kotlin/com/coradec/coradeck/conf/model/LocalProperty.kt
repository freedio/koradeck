/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.conf.model

import com.coradec.coradeck.conf.model.impl.ContextProperty
import com.coradec.coradeck.conf.model.impl.MandatoryContextProperty
import com.coradec.coradeck.core.util.caller
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
interface LocalProperty<P : Any> : Property<P> {
    companion object {
        inline operator fun <reified P : Any> invoke(name: String, default: P): Property<P> =
            ContextProperty(typeOf<P?>(), caller.propertyBase, name, default)
        inline operator fun <reified P : Any> invoke(name: String): Property<P> =
            MandatoryContextProperty(typeOf<P?>(), caller.propertyBase, name)
    }
}
