/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.gui.model.Component

interface DelegatedComponent: Component {
    /** The delegate to which all operations are delegated. */
    val delegate: ComponentDelegate
}
