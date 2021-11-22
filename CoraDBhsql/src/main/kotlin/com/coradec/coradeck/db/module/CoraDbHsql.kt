/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.module

import com.coradec.coradeck.db.model.impl.HsqlDatabase
import com.coradec.coradeck.type.model.Password
import java.net.URI

class CoraDbHsql: CoraDBAPI {
    override fun database(uri: URI, username: String, password: Password) = HsqlDatabase(uri, username, password).databaseView
}
