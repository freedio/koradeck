/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.db.model.Database
import kotlin.reflect.KClass

open class HsqlDbView<Record: Any>(
    db: Database,
    model: KClass<out Record>
) : HsqlDbCollection<Record>(db, model)
