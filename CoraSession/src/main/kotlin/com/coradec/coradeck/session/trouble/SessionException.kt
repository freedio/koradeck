package com.coradec.coradeck.session.trouble

import com.coradec.coradeck.core.trouble.BasicException

open class SessionException(message: String?, problem: Throwable?) : BasicException(message, problem) {
    constructor(message: String) : this(message, null)
}
