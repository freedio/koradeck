/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.type.converter

import com.coradec.coradeck.db.model.DatabaseParameters
import com.coradec.coradeck.type.ctrl.impl.BasicTypeConverter
import com.coradec.coradeck.type.model.Password
import java.net.URI

class com_coradec_coradeck_db_model_DatabaseParametersConverter() :
    BasicTypeConverter<DatabaseParameters>(DatabaseParameters::class) {
    override fun convertFrom(value: Any): DatabaseParameters? = when (value) {
        is Map<*, *> -> {
            val dbURI: URI = value["URI"]?.let { if (it is String) URI(it) else it as URI }
                ?: throw IllegalArgumentException("Missing ‹URI› in map!")
            val dbUsername: String = value["Username"] as? String ?: throw IllegalArgumentException("Missing ‹Username› in map!")
            val dbPassword: Password = value["Password"]?.let { if (it is String) Password(it) else it as Password }
                ?: throw IllegalArgumentException("Missing ‹Password› in map!")
            DatabaseParameters(dbURI, dbUsername, dbPassword)
        }
        else -> null
    }

    override fun decodeFrom(value: String): DatabaseParameters? = null
}
