/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicStackTrace

interface StackTrace {
    companion object {
        operator fun invoke(frames: List<StackFrame>) = BasicStackTrace(frames)
    }
}
