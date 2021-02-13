/*
 * Copyright © 2020 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.text.model

import com.coradec.coradeck.core.util.caller
import com.coradec.coradeck.text.model.impl.BasicConText

interface LocalText: ConText {
    companion object {
        operator fun invoke(name: String): ConText = BasicConText(caller.className, name)
    }
}
