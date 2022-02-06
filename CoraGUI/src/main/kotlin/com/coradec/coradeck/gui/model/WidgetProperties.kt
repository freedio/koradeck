/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import com.coradec.coradeck.core.model.Origin

interface WidgetProperties : Origin {
    /** The owner of the properties. */
    val owner: Widget
}
