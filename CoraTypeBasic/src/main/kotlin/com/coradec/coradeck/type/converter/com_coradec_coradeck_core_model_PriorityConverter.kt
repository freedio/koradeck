/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.core.model.Priority
import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter

class com_coradec_coradeck_core_model_PriorityConverter: BasicTypeConverter<Priority>(Priority::class) {
    override fun decodeFrom(value: String): Priority? = with (value.lowercase()) {
        Priority.values().singleOrNull { it.name == this }
    }

    override fun convertFrom(value: Any): Priority? = when (value) {
        is Int -> Priority.values().singleOrNull { it.ordinal == value }
        else -> null
    }
}
