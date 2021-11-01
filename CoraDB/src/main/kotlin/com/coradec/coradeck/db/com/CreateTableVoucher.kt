/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.com

import com.coradec.coradeck.com.model.impl.BasicVoucher
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.db.model.RecordTable
import kotlin.reflect.KClass

class CreateTableVoucher<Record : Any>(origin: Origin, val model: KClass<Record>): BasicVoucher<RecordTable<Record>>(origin)
