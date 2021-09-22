/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.dir.trouble

class CreateLockFailedException(message: String?, problem: Throwable?) : DirectoryLockingException(message, problem) {
    constructor(message: String) : this(message, null)
    constructor() : this(null, null)
}
