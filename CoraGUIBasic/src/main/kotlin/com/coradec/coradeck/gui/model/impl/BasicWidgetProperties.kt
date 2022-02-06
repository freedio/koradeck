/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.impl

import com.coradec.coradeck.gui.model.Widget
import com.coradec.coradeck.gui.model.WidgetProperties

open class BasicWidgetProperties(override val owner: Widget): WidgetProperties {
    override fun represent(): String = "%s.properties".format(owner)
}
