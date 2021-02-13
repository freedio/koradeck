package com.coradec.coradeck.ctrl.module

import com.coradec.coradeck.ctrl.ctrl.EMS
import com.coradec.coradeck.dir.model.module.CoraModuleAPI

interface CoraControlAPI: CoraModuleAPI {
    val EMS: EMS
}
