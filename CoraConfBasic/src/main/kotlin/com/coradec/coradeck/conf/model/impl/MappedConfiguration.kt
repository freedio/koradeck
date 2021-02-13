package com.coradec.coradeck.conf.model.impl

import com.coradec.coradeck.conf.model.Configuration
import com.coradec.coradeck.type.module.CoraType
import kotlin.reflect.KClass
import kotlin.reflect.KType

@Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING", "UNCHECKED_CAST")
open class MappedConfiguration(private val values: Map<String, Any>): Configuration {
    override fun <T : Any> get(type: KClass<T>, name: String): T? = values[name].let { value -> CoraType.castTo(value, type) }
    override fun <P: Any> get(type: KType, name: String): P? = type.classifier?.let { get(it as KClass<P>, name) }
}
