package com.coradec.coradeck.com.trouble

import com.coradec.coradeck.core.trouble.BasicException

open class CommunicationException(message: String?, problem: Throwable?) : BasicException(message, problem) {
    constructor(message: String) : this(message, null)
    constructor() : this(null, null)
    constructor(problem: Throwable) : this(null, problem)
}
