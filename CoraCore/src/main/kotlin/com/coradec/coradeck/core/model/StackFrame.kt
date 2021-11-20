/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model

import com.coradec.coradeck.core.model.impl.BasicStackFrame

interface StackFrame: Origin {
    val className: String
    val realClassName: String
    val propertyBase: String
    val methodName: String
    val fileName: String
    val lineNumber: Int

    companion object {
        operator fun invoke(frame: StackTraceElement, effective: Any? = null) = BasicStackFrame(frame, effective)
    }
}
