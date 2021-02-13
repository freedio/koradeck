/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.ctrl.ctrl.EMS
import com.coradec.coradeck.dir.model.module.CoraModule

object CoraControl : CoraModule<CoraControlAPI>() {
    val EMS: EMS = impl.EMS
}
