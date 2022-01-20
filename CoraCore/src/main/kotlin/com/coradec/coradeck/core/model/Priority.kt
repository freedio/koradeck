/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

enum class Priority : SqlTransformable {
    A0, A1, B0, A2, B1, C0, A3, B2, C1, B3, C2, C3;

    override fun toSqlValue(): String = "'$name'"

    companion object {
        val defaultPriority = B2
        val sqlType = "CHAR(2)"
        fun fromSql(value: String): Priority = values().singleOrNull { it.name == value } ?: throw NoSuchElementException(value)
        fun from(name: String): Priority = values().singleOrNull { it.name == name } ?: throw NoSuchElementException(name)
    }
}
