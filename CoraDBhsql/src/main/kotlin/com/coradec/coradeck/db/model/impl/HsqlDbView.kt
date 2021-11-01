/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model.impl

import com.coradec.coradeck.core.util.classname
import com.coradec.coradeck.db.ctrl.impl.SqlSelection
import com.coradec.coradeck.db.model.Database
import com.coradec.coradeck.db.model.RecordView
import kotlin.reflect.KClass

open class HsqlDbView<Record: Any>(
    db: Database,
    model: KClass<out Record>
) : HsqlDbCollection<Record>(db, model), RecordView<Record> {
    override val selector = SqlSelection.ALL
    override val recordName: String = model.classname
}
