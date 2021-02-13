package com.coradec.coradeck.type.trouble

import com.coradec.coradeck.core.trouble.BasicException

open class TypeException(message: String?, problem: Throwable?) : BasicException(message, problem) {
    constructor(message: String) : this(message, null)
    constructor() : this(null, null)
}
