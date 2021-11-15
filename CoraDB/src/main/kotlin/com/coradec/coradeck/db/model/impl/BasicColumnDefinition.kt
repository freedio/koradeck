/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.db.model.ColumnDefinition

data class BasicColumnDefinition(val sqlType: String, val nullable: Boolean) : ColumnDefinition {
    override fun toString() = "$sqlType${if (!nullable) " not null" else ""}"
}
