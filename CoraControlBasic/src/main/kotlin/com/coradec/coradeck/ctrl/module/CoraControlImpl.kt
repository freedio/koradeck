package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.ctrl.ctrl.EMS
import com.coradec.coradeck.ctrl.ctrl.impl.CEMS

class CoraControlImpl : CoraControlAPI {
    override val EMS: EMS get() = CEMS
}