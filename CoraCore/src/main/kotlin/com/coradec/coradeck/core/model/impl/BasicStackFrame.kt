package com.coradec.coradeck.core.model.impl

import com.coradec.coradeck.core.model.StackFrame

class BasicStackFrame(frame: StackTraceElement): StackFrame {
    override val className: String = frame.className
    override val realClassName: String = frame.className
    override val propertyBase: String = frame.className.removeSuffix("\$Companion")
    override val methodName: String = frame.methodName
    override val fileName: String = frame.fileName ?: "unknown"
    override val lineNumber: Int = frame.lineNumber

    override val representation: String = "\tat $className.$methodName ($fileName:$lineNumber)"
}
