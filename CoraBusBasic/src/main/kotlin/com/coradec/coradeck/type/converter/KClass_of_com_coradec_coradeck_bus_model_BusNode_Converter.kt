/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.bus.model.BusNode
import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
class KClass_of_com_coradec_coradeck_bus_model_BusNode_Converter() :
    BasicTypeConverter<KClass<BusNode>>(KClass::class as KClass<KClass<BusNode>>) {
    override fun convertFrom(value: Any): KClass<BusNode>? {
        TODO("Not yet implemented")
    }

    override fun decodeFrom(value: String): KClass<BusNode>? = Class.forName(value).kotlin.let {
        if (it.isSubclassOf(BusNode::class)) it as KClass<BusNode> else null
    }
}
