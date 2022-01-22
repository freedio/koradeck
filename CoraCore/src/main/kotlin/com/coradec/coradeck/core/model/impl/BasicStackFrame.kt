/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.StackFrame
import com.coradec.coradeck.core.util.classname

class BasicStackFrame(frame: StackTraceElement, effective: Any?): StackFrame {
    override val className: String = frame.className
    override val realClassName: String = effective?.classname ?: frame.className
    override val propertyBase: String = frame.className.removeSuffix("\$Companion")
    override val methodName: String = frame.methodName
    override val fileName: String = frame.fileName ?: "unknown"
    override val lineNumber: Int = frame.lineNumber

    override fun represent(): String = "\tat ${trace()}"
    override fun toString(): String = "BasicStackFrame[${trace()}]"
    private fun trace() =
        if (realClassName == className) "$className.$methodName($fileName:$lineNumber)"
        else "$className.$methodName($fileName:$lineNumber) in $realClassName"
}
