/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

import com.coradec.coradeck.gui.model.Label

interface LabelDelegate: ComponentDelegate, Label {
    /** The delegator. */
    override val delegator: LabelDelegator?
}
