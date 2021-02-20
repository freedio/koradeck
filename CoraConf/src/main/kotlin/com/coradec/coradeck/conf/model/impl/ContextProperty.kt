package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.conf.model.Property
import com.coradec.coradeck.core.util.caller
import kotlin.reflect.KType
import kotlin.reflect.typeOf

open class ContextProperty<P: Any>(
        type: KType,
        context: String,
        name: String,
        default: P
): DefaultNamedProperty<P>(name, type, default) {
    private val config = Configuration(context)
    override val value: P get() =
        config[type, name] ?: super.value

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        inline operator fun <reified P: Any> invoke(context: String, name: String, default: P): Property<P> =
            ContextProperty(typeOf<P?>(), context, name, default)
        inline operator fun <reified P: Any> invoke(context: String, name: String): Property<P> =
            MandatoryContextProperty(typeOf<P?>(), context, name)
    }
}
