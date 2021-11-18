/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.core.util.contains
import com.coradec.coradeck.db.model.ColumnDefinition

data class BasicColumnDefinition(
    override val sqlType: String,
    override val nullable: Boolean = false,
    override val primary: Boolean = false,
    override val generated: String? = null,
    override val always: Boolean = false
) : ColumnDefinition {
    private val notNullString get() = if (nullable || primary) "" else " not null"
    private val primaryKeyString get() = if (primary) " primary key" else ""
    private val genX get() = when (generated) {
        null -> ""
        "identity" -> generated
        in Regex("sequence .*") -> generated
        else -> "($generated)"
    }
    private val generatedString get() =
        if (generated == null) "" else " generated ${if(always) "always" else "by default"} as $genX"

    override fun toString() = "$sqlType$generatedString$primaryKeyString$notNullString"
}
