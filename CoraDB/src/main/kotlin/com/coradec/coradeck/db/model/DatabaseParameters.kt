/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.type.model.Password
import java.net.URI

data class DatabaseParameters(
    val URI: URI,
    val Username: String,
    val Password: Password
)
