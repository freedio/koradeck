package com.coradec.coradeck.dir.trouble

import com.coradec.coradeck.core.trouble.BasicException

open class DirectoryException(message: String?, problem: Throwable?) : BasicException(message, problem) {
    constructor(message: String) : this(message, null)
    constructor() : this(null, null)
}
