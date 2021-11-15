/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.db.model.impl.BasicColumnDefinition

interface ColumnDefinition {
    companion object {
        operator fun invoke(sqlType: String, nullable: Boolean): ColumnDefinition = BasicColumnDefinition(sqlType, nullable)
    }
}
