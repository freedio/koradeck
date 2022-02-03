/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.core.trouble

class ApplicationException(message: String?, problem: Throwable?): BasicException(message, problem) {
    constructor(message: String?) : this(message, null)
    constructor(problem: Throwable?) : this(null, problem)
    constructor() : this(null, null)
}
