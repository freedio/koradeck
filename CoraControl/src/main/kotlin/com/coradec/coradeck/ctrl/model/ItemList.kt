/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.com.ctrl.Observer
import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.MultiRequest
import com.coradec.coradeck.core.model.Origin
import com.coradec.coradeck.ctrl.module.CoraControl

interface ItemList: Observer, MultiRequest {
    companion object {
        operator fun invoke(origin: Origin, vararg items: Information) =
                CoraControl.createItemList(origin, *items)
    }
}