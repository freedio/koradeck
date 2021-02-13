package com.coradec.coradeck.dir.trouble

open class DirectoryLockingException(message: String?, problem: Throwable?): DirectoryException(message, problem) {
    constructor() : this(null, null)
    constructor(message: String) : this(message, null)
}
