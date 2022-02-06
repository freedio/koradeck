/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import java.awt.Color

interface ComponentProperties: WidgetProperties {
    /** Whether the component is visible. */
    var visible: Boolean
    /** Whether the component is enabled. */
    var enabled: Boolean
    /** Whether the component is editable (mutable, changeable). */
    var editable: Boolean

    /** The component's offset from the top in the container, or absent it the absolute Y coordinate. */
    var top: Int
    /** The component's offset from the left in the container, or absent it the absolute X coordinate. */
    var left: Int
    /** The height of the component. */
    var height: Int
    /** The width of the component. */
    var width: Int
    /** The component's foreground color (inherited if `null`). */
    var foregroundColor: Color?
    /** The component's background color (inherited if `null`). */
    var backgroundColor: Color?
}
