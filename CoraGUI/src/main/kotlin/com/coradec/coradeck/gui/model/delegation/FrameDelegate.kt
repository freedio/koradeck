/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.gui.model.Frame

interface FrameDelegate: WindowDelegate, Frame {
    /** The delegator. */
    override val delegator: FrameDelegator?
}
