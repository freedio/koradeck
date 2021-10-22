/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.ctrl.model

import com.coradec.coradeck.com.model.Information

interface StateTransition {
    /** Returns the end state of the transition. */
    val nextState: State

    /** Presents the specified information and checks if the transition is ready after it. */
    fun isReadyOn(info: Information): Boolean
}
