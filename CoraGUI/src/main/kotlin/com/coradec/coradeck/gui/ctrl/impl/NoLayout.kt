/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.ctrl.impl

import java.awt.Container
import java.awt.Dimension

class NoLayout : BasicLayout() {
    private val nodim = Dimension(0, 0)
    override fun preferredLayoutSize(parent: Container): Dimension = nodim
    override fun minimumLayoutSize(parent: Container): Dimension = nodim
    override fun maximumLayoutSize(target: Container): Dimension = nodim
    override fun getLayoutAlignmentX(target: Container): Float = Float.NaN
    override fun getLayoutAlignmentY(target: Container): Float = Float.NaN
    override fun layoutContainer(parent: Container) {}
}
