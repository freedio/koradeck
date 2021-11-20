/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.bus.view

import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.session.view.View

interface MemberView : View {
    fun attach(context: BusContext): Request
    fun standby()
    fun detach(): Request
}
