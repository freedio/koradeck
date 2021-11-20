/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

interface ColumnDefinition {
    val sqlType: String
    val nullable: Boolean
    val primary: Boolean
    val indexed: Boolean
    val generated: String?
    val always: Boolean
}
