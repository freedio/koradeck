/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.module

import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.module.model.CoraModule
import com.coradec.coradeck.type.model.Password

object CoraDB: CoraModule<CoraDBAPI>() {
    /** Opens the database with the specified URI for the specified user with the specified password. */
    fun database(uri: String, username: String, password: Password): Database = impl.database(uri, username, password)
}
