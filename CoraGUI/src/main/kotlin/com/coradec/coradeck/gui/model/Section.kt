/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import com.coradec.coradeck.gui.ctrl.Layout

interface Section {
    /** The section layout. */
    val layout: Layout

    /** Adds the specified component to the section under the specified name. */
    fun add(component: Component)
}
