/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

import com.coradec.coradeck.bus.model.BusHub
import java.sql.Connection

interface Database: BusHub {
    val connection: Connection

    fun close()
}
