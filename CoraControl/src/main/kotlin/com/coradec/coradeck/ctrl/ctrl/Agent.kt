/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.ctrl

import com.coradec.coradeck.com.model.Information
import com.coradec.coradeck.com.model.Message
import com.coradec.coradeck.com.model.Recipient
import com.coradec.coradeck.core.model.Origin

interface Agent: Origin, Recipient {
    /** Injects the specified information. */
    fun <I : Information> accept(info: I): Message<I>
    /** Waits until all requests so far have been processed. */
    fun synchronize()
}
