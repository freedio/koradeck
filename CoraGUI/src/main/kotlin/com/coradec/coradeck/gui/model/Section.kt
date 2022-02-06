/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import com.coradec.coradeck.bus.view.MemberView
import com.coradec.coradeck.com.model.Request
import com.coradec.coradeck.com.model.Voucher

interface Section {
    fun add(name: String, component: MemberView): Request
    fun contains(name: String): Voucher<Boolean>
}
