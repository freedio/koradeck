/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin

interface Agent: Origin, Recipient {
    /** Waits until all requests so far have been processed. */
    fun synchronize()
}
