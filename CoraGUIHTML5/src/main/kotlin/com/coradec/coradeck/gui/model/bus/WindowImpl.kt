/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.bus

import com.coradec.coradeck.gui.model.delegation.WindowDelegate
import com.coradec.coradeck.gui.model.delegation.WindowDelegator

open class WindowImpl(override val delegator: WindowDelegator? = null) : ContainerImpl(delegator), WindowDelegate {
    private val owner get() = delegator?.owner
}
