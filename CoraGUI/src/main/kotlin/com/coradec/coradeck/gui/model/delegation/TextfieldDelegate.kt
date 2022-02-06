/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model.delegation

interface TextfieldDelegate: ComponentDelegate {
    /** The delegator. */
    override val delegator: TextfieldDelegator?
}
